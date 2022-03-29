from collections import defaultdict
from data_access.compunded_watch_logs import CompoundedWatchLogs
from data_access.recommendation_result import RecommendationResult
from data_access.db import get_watching_log_after_recent_recommendation_requests
from cos_sim.cos_sim import generate_cos_sim_matrix

import numpy as np

class OnlineEvaluator:
    def __init__(self):
        self.recommendation_map = {}
        self.watch_logs = defaultdict(dict)
        query_results = get_watching_log_after_recent_recommendation_requests()
        for result in query_results:
            user_id, movie_title, _, rec_id, _, recommendations, minute, response_time = result
            recommendations = list(map(lambda x: x.strip(), recommendations))
            
            # TODO: temporary care for a bug in recommendation request parsing
            recommendations = recommendations[:-1] 

            movie_title = movie_title.strip()
            self.recommendation_map[rec_id] = RecommendationResult(user_id, recommendations, response_time)
            if movie_title not in self.watch_logs[rec_id]:
                self.watch_logs[rec_id][movie_title] = CompoundedWatchLogs(user_id, movie_title)
            self.watch_logs[rec_id][movie_title]._add_new_minute(minute)

    
    def evaluate(self):
        # print(self.recommendation_map)
        # print(self.calculate_avg_response_time())
        
        return {
            "cos_sim": self.calculate_cos_sim(), 
            "avg_response_time": self.calculate_avg_response_time()
        }

    def calculate_avg_response_time(self):
        response_times = 0
        for rec in self.recommendation_map.values():
            response_times += rec.response_time
        
        return response_times / len(self.recommendation_map)
    
    def calculate_cos_sim(self):
        (rec_title_to_index_map, watched_title_to_index_map, cos_sim_matrix) = generate_cos_sim_matrix(self.recommendation_map, self.watch_logs)
        max_cos_sims = []
        for k,v in self.recommendation_map.items():
            recommendations = v.results
            watched_movies = list(self.watch_logs[k].keys())
            cos_sims = []
            for rec in recommendations:
                for watched in watched_movies:
                    row = rec_title_to_index_map.get(rec, None)
                    col = watched_title_to_index_map.get(watched, None)
                    if row is not None and col is not None:
                        cos_sims.append(cos_sim_matrix[row, col])
            max_cos_sims.append(max(cos_sims))
        
        return np.mean(max_cos_sims)
    
    def calculate_minute_count(self):
        return