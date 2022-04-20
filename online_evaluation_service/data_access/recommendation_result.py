class RecommendationResult:
    def __init__(self, user_id, results, response_time):
        self.user_id = user_id
        self.results = results
        self.response_time = response_time
    
    def __repr__(self):
        return str(self.user_id) + ": " + str(self.results)