import os, time
from pathlib import Path
path = str(Path(__file__).parent.absolute())
from surprise import SVD, SVDpp, KNNBaseline
from data_access.db import get_user_ratings, get_movie_info
from model_inference.inference import CF_inference_fast, content_based_hybrid_inference_fast, content_based_hybrid_inference_fast_avg_rating_pred
from model_training.simple_model import collaborative_filtering, content_based_filtering

if __name__ == '__main__':
    test_userids = [146034, 130187, 141420, 115573, 119455, 58497, 135115, 114793, 149848, 45559]
    user_rating_df = get_user_ratings()
    movie_info_df = get_movie_info()

    for sample_frac in [0.01, 0.1, 0.5, 1.0]:
        print('==========================================')
        print('Sample fraction; {}'.format(sample_frac))
        print('==========================================')
        for algo in [SVD, SVDpp, KNNBaseline]:
            print('------------------------------------------')
            print(algo.__name__)
            print('------------------------------------------')
            start_training_time = time.time()
            user_rating_sample = user_rating_df.sample(frac=sample_frac)
            avg_rmse, avg_mae = collaborative_filtering(SVD, user_rating_sample)
            finish_training_time = time.time()
            for id in test_userids:
                CF_inference_fast(algo.__name__, id)
            finish_inference_time = time.time()
            print('The prediction accuracy metrics of {} algorithm: average RMSE: {}, average MAE: {}'.format(algo.__name__,
                                                                                                              avg_rmse.mean(),
                                                                                                              avg_mae.mean()))
            print('The training cost metric of {} algorithm: training time: {} s'.format(algo.__name__,
                                                                                         finish_training_time - start_training_time))
            print('The inference cost metric of {} algorithm: average inference time: {} s'.format(algo.__name__,
                                                                                                   (finish_inference_time - finish_training_time)/len(test_userids)))
            print('The model size metric of {} algorithm: pickled model file size: {} bytes'.format(algo.__name__,
                                                                                              os.path.getsize(path+'/pickled_objects/{}.pkl'.format(algo.__name__))))
        print('------------------------------------------')
        print('Content-based filtering')
        print('------------------------------------------')
        start_training_time = time.time()
        movie_info_sample = movie_info_df.sample(frac=sample_frac)
        content_based_filtering(movie_info_sample)
        finish_training_time = time.time()
        movie_titles_recommends = []
        for id in test_userids:
            content_based_hybrid_inference_fast(id)
        finish_inference_time = time.time()
        avg_rating = 0
        for id in test_userids:
            avg_rating += content_based_hybrid_inference_fast_avg_rating_pred(id)
        print('The prediction accuracy metrics of content based filtering: recommended movies average predicted rating: {}'.format(avg_rating/len(test_userids)))
        print('The training cost metric of content based filtering: training time: {} s'.format(finish_training_time - start_training_time))
        print('The inference cost metric of content based filtering: average inference time: {} s'.format((finish_inference_time - finish_training_time) / len(test_userids)))
        print('The model size metric of content based filtering: pickled model file size: {} bytes'.format(os.path.getsize(
                                                                                                    path + '/pickled_objects/content_based_model.pkl')))