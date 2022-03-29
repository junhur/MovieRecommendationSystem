import numpy as np
import json 
from sklearn.metrics.pairwise import cosine_similarity
from data_access.db import get_movie_info_from_titles

NUMERIC_VALUES = 5
NUM_FEATURE = 25

GENRES = [
    'Action', 'Adventure', 'Animation', 'Comedy', 'Crime', 'Documentary', 
    'Drama', 'Family', 'Fantasy', 'Foreign', 'History', 'Horror', 'Music',       
    'Mystery', 'Romance', 'Science Fiction', 'TV Movie', 'Thriller', 'War',       
    'Western'    
]

GENRES_MAP = {GENRES[i] : i + NUMERIC_VALUES for i in range(len(GENRES))}

def generate_cos_sim_matrix(recommendation_map, watch_logs):
    (rec_title_to_index_map, 
     rec_infos, 
     watched_title_to_index_map, 
     watched_infos) = _get_all_movies(recommendation_map, watch_logs)

    rec_feature_vectors = list(map(feature_vectorize, rec_infos))
    watched_feature_vectors = list(map(feature_vectorize, watched_infos))
    rec_matrix = np.stack(rec_feature_vectors)
    watched_matrix = np.stack(watched_feature_vectors)

    return (rec_title_to_index_map, watched_title_to_index_map, cosine_similarity(rec_matrix, watched_matrix))

def _get_all_movies(recommendation_map, watch_logs):
    recommended_movies = set()
    watched_movies = set()
    for rec_id in recommendation_map:
        recommended_movies.update(set(recommendation_map[rec_id].results))
        watched_movies.update(watch_logs[rec_id].keys())
    
    movie_infos = get_movie_info_from_titles(recommended_movies, watched_movies)
    
    rec_title_to_index_map, rec_infos = movie_infos["rec"]
    watched_title_to_index_map, watched_infos = movie_infos["watched"]
    
    return (rec_title_to_index_map, rec_infos, 
            watched_title_to_index_map, watched_infos)

def feature_vectorize(movie_info):
    feature_vector = np.zeros(NUM_FEATURE)
    numeric_values = movie_info[:NUMERIC_VALUES]

    for i in range(len(numeric_values)):
        feature_vector[i] = float(numeric_values[i])
    
    genres = movie_info[-1]
    genres = json.loads(genres)
    for genre in genres:
        idx = GENRES_MAP[genre["name"]]
        feature_vector[idx] = 1.0
    return feature_vector

