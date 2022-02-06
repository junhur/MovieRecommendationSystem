import os
import itertools
import dill as pickle
import psycopg2
import pandas as pd

from surprise import Dataset
from surprise import Reader
from surprise.model_selection import cross_validate, GridSearchCV
from surprise import BaselineOnly, NormalPredictor, KNNBasic, SVD

from concurrent.futures import ThreadPoolExecutor

## DB information
DB_HOST = 'localhost'
DB_PORT = 5432
DB_NAME = 'movielog'
DB_USER = 'xiangyuy'
DB_PSWD = 'yxy1996630'


def create_dummy_model(movie_ids, model_path):
	def dummy_model(user_id): return movie_ids
	with open(model_path+'dummy.pkl', 'wb') as file:
		pickle.dump(dummy_model, file)

def get_most_popular_movie_ids(num_ids):
	conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
	cur = conn.cursor()
	cur.execute('''SELECT title
					FROM movies
				   ORDER BY (info->>'popularity')::NUMERIC DESC
				   FETCH FIRST {} ROWS ONLY'''.format(num_ids))
	query_results = cur.fetchall()
	cur.close()
	conn.close()
	return [q[0] for q in query_results]

def get_user_ratings():
	conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
	cur = conn.cursor()
	cur.execute('''SELECT user_id, movie_title, score
					FROM rating''')
	query_results = cur.fetchall()
	cur.close()
	conn.close()
	return pd.DataFrame(query_results, columns=['user_id', 'movie_title', 'score'])


if __name__ == '__main__':
	trained_model_path = '../model_trained/'

	## ------------------
	## Create dummy model 
	##-------------------
	if not os.path.isfile(trained_model_path + 'dummy.pkl'):
		highest_rated_20_movie_ids = get_most_popular_movie_ids(20)
		try:
			create_dummy_model(highest_rated_20_movie_ids, trained_model_path)
		except Exception:
			print('dummy model file generation failed')

	## ----------------------------------------------
	## Training surprise models with current database
	##-----------------------------------------------
	# Algorithm configurations
	algo = SVD()
	metrics = ['RMSE', 'MAE']
	cv_fold = 5
	param_grid = {'n_epochs': [5, 10], 'lr_all': [0.002, 0.005], 'reg_all': [0.4, 0.6]}
	pred_num_threads = 4
	
	# Loading data
	data_df = get_user_ratings()
	reader = Reader(rating_scale=(1, 5))
	data = Dataset.load_from_df(data_df, reader)
	
	# Training with cross validation
	cross_validate(algo, data, measures=metrics, cv=cv_fold, verbose=False)
	
	# # Hyperparameter training with grid search
	# gs = GridSearchCV(SVD, param_grid, measures=metrics, cv=cv_fold)
	# gs.fit(data)
	# print(gs.best_score['rmse'])
	# print(gs.best_params['rmse'])
	# algo = gs.best_estimator['rmse']
	# algo.fit(data.build_full_trainset())

	# Building the recommender
	def recommender(user_id):
		if user_id in data_df['user_id'].unique():
			user_preds = []
			def predict_user(title):
				user_preds.append((title, algo.predict(user_id, title).est))
			with ThreadPoolExecutor(pred_num_threads) as executor:
				executor.map(predict_user, data_df['movie_title'].unique())
			user_preds_df = pd.DataFrame(user_preds, columns=['movie_title', 'pred_score'])
			user_preds_top_20 = user_preds_df.sort_values('pred_score', ascending=False).head(20)
			return user_preds_top_20.movie_title.tolist()
		else:
			return get_most_popular_movie_ids(20)

	print(recommender(91431))

	# # Pickle the trained model
	# with open(trained_model_path+'surprise_SVD.pkl', 'wb') as file:
	# 	pickle.dump(recommender, file)
