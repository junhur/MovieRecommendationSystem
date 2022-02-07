import dill as pickle
import pandas as pd
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import sys

path = str(Path(Path(__file__).parent.absolute()).parent.absolute())
sys.path.insert(0, path)
from model_training.db import get_most_popular_movie_ids, get_user_ratings

# Inference information
trained_model_path = path+'/pickled_objects/'
pred_num_threads = 4


def SVD_inference(user_id):
    data_df = get_user_ratings()
    if user_id in data_df['user_id'].unique():
        with open(trained_model_path + 'SVD.pkl', 'rb') as file:
            algo = pickle.load(file)
        user_preds = []

        def predict_user(title):
            user_preds.append((title, algo.predict(user_id, title).est))

        with ThreadPoolExecutor(pred_num_threads) as executor:
            executor.map(predict_user, data_df['movie_title'].unique())
        user_preds_df = pd.DataFrame(user_preds, columns=['movie_title', 'pred_score'])
        user_preds_top_20 = user_preds_df.sort_values('pred_score', ascending=False).head(20)
        return user_preds_top_20['movie_title'].tolist()
    else:
        return dummy_inference(user_id)


def dummy_inference(user_id):
    return get_most_popular_movie_ids(20)


if __name__ == '__main__':
    print(dummy_inference(146034))
    print(SVD_inference(146034))
