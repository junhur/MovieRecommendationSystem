import psycopg2
import pandas as pd

with open("secrets.txt") as f:
    lines = f.readlines()
    DB_HOST = lines[0].strip()
    DB_PORT = int(lines[1])
    DB_NAME = lines[2].strip()
    DB_USER = lines[3].strip()
    DB_PSWD = lines[4].strip()

## DB information
# DB_HOST = 'favor8-pg.postgres.database.azure.com'
# DB_PORT = 5432
# DB_NAME = 'postgres'
# DB_USER = 'favor8'
# DB_PSWD = 'Cmu17645!'

def get_movie_info_from_titles(recommended, watched):
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()

    cur.execute('''
        SELECT
            title,
            info->>'budget' as budget,
            info->>'revenue' as revenue,
            info->>'runtime' as runtime,
            info->>'popularity' as popularity,
            info->>'vote_average' as vote_average,
            info->>'genres' as genres
        FROM movies
        WHERE title IN %s
        OR title IN %s;
    ''', (tuple(recommended), tuple(watched), ))

    movie_results = cur.fetchall()
    cur.close()
    conn.close()

    rec_title_to_index_map = {}
    rec_infos = []
    rec_index = 0
    watched_title_to_index_map = {}
    watched_infos = []
    watched_index = 0
    
    for result in movie_results:
        title = result[0]
        info = result[1:]
        if title in recommended:
            rec_title_to_index_map[title] = rec_index
            rec_infos.append(info)
            rec_index += 1
        if title in watched:
            watched_title_to_index_map[title] = watched_index
            watched_infos.append(info)
            watched_index += 1
    
    
    return {"rec": (rec_title_to_index_map, rec_infos), "watched": (watched_title_to_index_map, watched_infos)}

def get_watching_log_after_recent_recommendation_requests():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''
        SELECT w.user_id, w.movie_title, w.watched_at, rr.id, rr.requested_at, rr.results, w.minute, rr.response_time
        FROM watching w
        JOIN (SELECT * FROM recommendation_request ORDER BY requested_At DESC LIMIT 1000) rr ON rr.user_id = w.user_id
        WHERE w.watched_at > rr.requested_at
        ORDER BY w.user_id, w.movie_title, w.minute;
    ''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return query_results

def get_most_popular_movie_ids(num_ids):
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT title
					FROM movies
				   ORDER BY (info->>'popularity')::NUMERIC DESC
				   FETCH FIRST {} ROWS ONLY'''.format(num_ids))
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return [q[0] for q in query_results]


def get_movie_info():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT title, info->>'original_title',
                          (info->>'adult')::BOOLEAN, (info->>'budget')::NUMERIC, (info->>'revenue')::NUMERIC, info->>'runtime', 
                          (info->>'popularity')::NUMERIC, (info->>'vote_count')::NUMERIC, (info->>'vote_average')::NUMERIC,
                          info->>'release_date', info->>'original_language', info->>'overview',
                          info->>'genres', info->>'production_companies', info->>'production_countries'
    			   FROM movies''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results,
                        columns=['movie_title', 'original_title', 'adult', 'budget', 'revenue', 'runtime', 'popularity',
                                 'vote_count',
                                 'vote_average',
                                 'release_date', 'original_language', 'overview', 'genres_json', 'companies_json',
                                 'countries_json'])


def get_user_ratings():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT user_id, movie_title, score
				   FROM rating''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'movie_title', 'score'])


def get_user_info():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT id, age, occupation, gender
    				   FROM users''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'user_age', 'user_occupation', 'user_gender'])


def get_user_watching_history():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''SELECT user_id, movie_title, MIN(minute), MAX(minute), (info->>'runtime')::NUMERIC 
    				   FROM watching LEFT JOIN movies ON watching.movie_title=movies.title
    				   GROUP BY user_id, movie_title, info->>'runtime' ''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return pd.DataFrame(query_results, columns=['user_id', 'movie_title', 'start_time', 'end_time', 'runtime'])


if __name__ == '__main__':
    print(get_most_popular_movie_ids(100))
