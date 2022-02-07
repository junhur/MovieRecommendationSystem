import dill as pickle

from surprise import Dataset
from surprise import Reader
from surprise.model_selection import cross_validate, GridSearchCV
from surprise import SVD

from db import get_user_ratings

from pathlib import Path

path = str(Path(Path(__file__).parent.absolute()).parent.absolute())
trained_model_path = path + '/pickled_objects/'

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

    # Pickle the predictive model
    try:
        with open(trained_model_path + '{}.pkl'.format(algo_name), 'wb') as file:
            pickle.dump(algo, file)
    except Exception:
        print('{} model file generation failed'.format(algo_name))
