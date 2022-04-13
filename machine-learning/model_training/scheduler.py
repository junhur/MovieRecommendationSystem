import argparse
import subprocess
import time
from datetime import datetime

import schedule

from data_access.db import get_user_ratings, record_model_metadata, get_latest_model_version
from model_training.simple_model import train_model


def get_git_hash():
    '''
    Get the current git hash
    '''
    try:
        return subprocess.check_output(['git', 'rev-parse', 'HEAD']).decode('ascii').strip()
    except Exception:
        return 'unknown'


def retrain_model(args=None):
    '''
    Retrain the model
    '''
    # Get the user ratings
    user_ratings = get_user_ratings()
    # get number of rows in the ratings data
    num_ratings = user_ratings.shape[0]
    # get latest git hash code
    git_hash = get_git_hash()
    # get current time
    current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    # generate model version
    latest_version = 0 if get_latest_model_version() is None else get_latest_model_version()
    model_version = latest_version + 1
    # Train the model
    train_model(args, data=user_ratings, version=model_version)
    # Record the model metadata to the provenance table
    record_model_metadata(model_version, git_hash, num_ratings, current_time)


if __name__ == '__main__':
    # get arguments
    parser = argparse.ArgumentParser()
    parser.add_argument('-a', '--algo', type=str, default='svd', help='algorithm name')
    parser.add_argument('-n', '--n_rec', type=int, default=20, help='number of recommendations')
    parser.add_argument('-t', '--hp_tune', type=bool, default=False, help='hyperparameter tuning')
    parser.add_argument('-c', '--cv_fold', type=int, default=5, help='number of cross validation folds')
    args = parser.parse_args()

    '''
    Schedule the model retraining
    run the retrain_model function every day at 12:00 AM
    
    on a linux environment, you can use the following command to schedule the job
    $ nohup python3 scheduler.py &
    a log file will be generated in the current directory with the name 'nohup.out'
    '''
    schedule.every().day.at("00:00").do(retrain_model, args)
    while True:
        schedule.run_pending()
        time.sleep(100)
