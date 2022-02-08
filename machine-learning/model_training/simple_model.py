import copy
import json
import sys
from collections import defaultdict

import dill as pickle
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel

from surprise import Dataset
from surprise import Reader
from surprise.model_selection import cross_validate, GridSearchCV
from surprise import SVD, SVDpp, KNNBaseline

from pathlib import Path
path = str(Path(Path(__file__).parent.absolute()).parent.absolute())
sys.path.insert(0, path)
from model_training.db import get_user_ratings, get_movie_info, get_user_watching_history

trained_model_path = path + '/pickled_objects/'


def cf_get_top_n(predictions, n=10):
    # https://surprise.readthedocs.io/en/stable/FAQ.html#how-to-get-the-top-n-recommendations-for-each-user
    # First map the predictions to each user.
    top_n = defaultdict(list)
    for uid, iid, true_r, est, _ in predictions:
        top_n[uid].append((iid, est))

    # Then sort the predictions for each user and retrieve the k highest ones.
    for uid, user_ratings in top_n.items():
        user_ratings.sort(key=lambda x: x[1], reverse=True)
        top_n[uid] = user_ratings[:n]

    return top_n


def collaborative_filtering(algo_f, data_df=None, extra_df=False, n_rec=20, hp_tune=False, cv_fold=5, metrics=None):
    '''
    Explicit collaborative filtering with surprise
    :param algo_f: algorithms supported by surprise
    :param n_rec: number of recommended movies
    :param hp_tune: flag to tune the hyperparameters with grid search
    :param cv_fold: cross validation folds
    :param metrics: training metrics
    '''
    # Algorithm configurations
    if metrics is None:
        metrics = ['RMSE', 'MAE']
    param_grid = {'n_epochs': [5, 10], 'lr_all': [0.002, 0.005], 'reg_all': [0.4, 0.6]}

    algo = algo_f()
    algo_name = algo_f.__name__

    # Loading data and creating datasets
    if data_df is None:
        data_df = get_user_ratings()
        if extra_df:
            extra_df = get_user_watching_history()
            extra_df = extra_df.loc[(extra_df['runtime'] > 0) & (extra_df['start_time'] >= 0) & (extra_df['end_time'] >= 0)]
            extra_df['score'] = 5 * (extra_df['end_time'] - extra_df['start_time']) / extra_df['runtime']
            data_df = data_df.append(extra_df[['user_id', 'movie_title', 'score']], ignore_index=True)
    reader = Reader(rating_scale=(1, 5))
    data = Dataset.load_from_df(data_df, reader)
    trainset = data.build_full_trainset()
    testset = trainset.build_anti_testset()

    # Training model
    if hp_tune:
        gs = GridSearchCV(algo_f, param_grid, measures=metrics, cv=cv_fold)
        gs.fit(data)
        algo = gs.best_estimator[metrics[0]]
        algo.fit(trainset)
    else:
        pred_accuracy = cross_validate(algo, data, measures=metrics, cv=cv_fold, verbose=False)

    # Than predict ratings for all pairs (u, i) that are NOT in the training set.
    predictions = algo.test(testset)
    top_n = cf_get_top_n(predictions, n=n_rec)

    # Pickle the predictive model
    try:
        with open(trained_model_path + '{}.pkl'.format(algo_name), 'wb') as file:
            pickle.dump(algo, file)
    except Exception:
        print('{} model file generation failed'.format(algo_name))

    # Pickle the dataset
    try:
        with open(trained_model_path + '{}_data.pkl'.format(algo_name), 'wb') as file:
            pickle.dump(data_df, file)
    except Exception:
        print('{} model data file generation failed'.format(algo_name))

    # Pickle the prediction results
    try:
        with open(trained_model_path + '{}_preds.pkl'.format(algo_name), 'wb') as file:
            pickle.dump(top_n, file)
    except Exception:
        print('{} model prediction file generation failed'.format(algo_name))

    return pred_accuracy['test_rmse'], pred_accuracy['test_mae']


def content_based_filtering(data_df=None):
    '''
    Simple content based filtering with scipy
    '''
    if data_df is None:
        data_df = get_movie_info()
    else:
        data_df = copy.deepcopy(data_df)
    content_labels = ['movie_title', 'original_title', 'overview', 'genres_json', 'companies_json', 'countries_json']
    data_df = data_df[content_labels]
    data_df['genres_json'] = data_df['genres_json'].apply(lambda x: ' '.join([a['name'] for a in json.loads(x)]))
    data_df['companies_json'] = data_df['companies_json'].apply(lambda x: ' '.join([a['name'] for a in json.loads(x)]))
    data_df['countries_json'] = data_df['countries_json'].apply(
        lambda x: ' '.join([a['iso_3166_1'] for a in json.loads(x)]))
    data_df['combined'] = data_df[content_labels[1:]].apply(lambda row: ' '.join(row.values.astype(str)), axis=1)
    tf = TfidfVectorizer(analyzer='word', ngram_range=(1, 2), min_df=0, stop_words='english')
    tfidf_matrix = tf.fit_transform(data_df['combined'])
    cosine_sim = linear_kernel(tfidf_matrix, tfidf_matrix)
    data_df = data_df.reset_index()
    titles = data_df['movie_title']
    indices = pd.Series(data_df.index, index=data_df['movie_title'])

    try:
        with open(trained_model_path + 'content_based_model.pkl', 'wb') as file:
            pickle.dump(cosine_sim, file)
    except Exception:
        print('content based model file generation failed')

    try:
        with open(trained_model_path + 'content_based_indices.pkl', 'wb') as file:
            pickle.dump(indices, file)
    except Exception:
        print('content based indices file generation failed')

    try:
        with open(trained_model_path + 'content_based_titles.pkl', 'wb') as file:
            pickle.dump(titles, file)
    except Exception:
        print('content based titles file generation failed')


if __name__ == '__main__':
    collaborative_filtering(SVD)
    collaborative_filtering(SVDpp)
    collaborative_filtering(KNNBaseline)
    content_based_filtering()
