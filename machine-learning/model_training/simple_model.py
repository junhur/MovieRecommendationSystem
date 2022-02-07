from collections import defaultdict

import dill as pickle

from surprise import Dataset
from surprise import Reader
from surprise.model_selection import cross_validate, GridSearchCV
from surprise import SVD

from db import get_user_ratings

from pathlib import Path

path = str(Path(Path(__file__).parent.absolute()).parent.absolute())
trained_model_path = path + '/pickled_objects/'


def get_top_n(predictions, n=10):
    # https://surprise.readthedocs.io/en/stable/FAQ.html#how-to-get-the-top-n-recommendations-for-each-user
    """Return the top-N recommendation for each user from a set of predictions.

    Args:
        predictions(list of Prediction objects): The list of predictions, as
            returned by the test method of an algorithm.
        n(int): The number of recommendation to output for each user. Default
            is 10.

    Returns:
    A dict where keys are user (raw) ids and values are lists of tuples:
        [(raw item id, rating estimation), ...] of size n.
    """

    # First map the predictions to each user.
    top_n = defaultdict(list)
    for uid, iid, true_r, est, _ in predictions:
        top_n[uid].append((iid, est))

    # Then sort the predictions for each user and retrieve the k highest ones.
    for uid, user_ratings in top_n.items():
        user_ratings.sort(key=lambda x: x[1], reverse=True)
        top_n[uid] = user_ratings[:n]

    return top_n


if __name__ == '__main__':
    # ----------------------------------------------
    # Training surprise models with current database
    # ----------------------------------------------
    # Algorithm configurations
    algo = SVD()
    algo_name = 'SVD'
    metrics = ['RMSE', 'MAE']
    cv_fold = 5
    hyperparameter_tuning = False
    param_grid = {'n_epochs': [5, 10], 'lr_all': [0.002, 0.005], 'reg_all': [0.4, 0.6]}

    # Loading data
    data_df = get_user_ratings()
    reader = Reader(rating_scale=(1, 5))
    data = Dataset.load_from_df(data_df, reader)

    # Training model
    if hyperparameter_tuning:
        gs = GridSearchCV(SVD, param_grid, measures=metrics, cv=cv_fold)
        gs.fit(data)
        algo = gs.best_estimator['rmse']
        algo.fit(data.build_full_trainset())
    else:
        cross_validate(algo, data, measures=metrics, cv=cv_fold, verbose=False)

    # Than predict ratings for all pairs (u, i) that are NOT in the training set.
    trainset = data.build_full_trainset()
    testset = trainset.build_anti_testset()
    predictions = algo.test(testset)
    top_n = get_top_n(predictions, n=20)

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
