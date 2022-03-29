class CompoundedWatchLogs:
    def __init__(self, user_id, movie_title):
        self.user_id = user_id
        self.movie_title= movie_title
        self.minutes = set()
    
    def _add_new_minute(self, minute):
        self.minutes.add(minute)
    
    def get_movie_title(self):
        return self.movie_title
    
    def get_minute_count(self):
        return len(self.minutes)

    def __repr__(self):
        return str(len(self.minutes))