import psycopg2

with open("secrets.txt") as f:
    lines = f.readlines()
    DB_HOST = lines[0].strip()
    DB_PORT = int(lines[1])
    DB_NAME = lines[2].strip()
    DB_USER = lines[3].strip()
    DB_PSWD = lines[4].strip()
    print(DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PSWD)

def get_watching_log_after_recent_recommendation_requests():
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, database=DB_NAME, user=DB_USER, password=DB_PSWD)
    cur = conn.cursor()
    cur.execute('''
        SELECT w.user_id, w.movie_title, w.watched_at, rr.id, rr.requested_at, rr.results, w.minute, rr.response_time
        FROM watching2 w
        JOIN (SELECT id, user_id, requested_at, results, response_time FROM recommendation_request ORDER BY id DESC LIMIT 3000) rr ON rr.user_id = w.user_id
        WHERE w.watched_at > rr.requested_at
        ORDER BY w.user_id, w.movie_title, w.minute;
    ''')
    query_results = cur.fetchall()
    cur.close()
    conn.close()
    return query_results

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
