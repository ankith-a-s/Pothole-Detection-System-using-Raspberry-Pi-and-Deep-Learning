#!/usr/bin/python
#database stuff
#includes code for getting data from table on heroku
import psycopg2, json

def create_db():
	conn = psycopg2.connect(database = "d4em1jlog8luec", user = "xprroouagkusrk",
                            password = "f4cc19c667c530477e935a62d84ebe50d4235262f6134590ba3ca53b53c3e4d2",
                            host = "ec2-184-72-238-22.compute-1.amazonaws.com", port = "5432")
	#print("Opened database successfully")
	cur = conn.cursor()
	cur.execute('''CREATE TABLE location (latitude double precision     NOT NULL,longitude double precision    NOT NULL);''')
	conn.commit()


#to insert to database
def insert_db():
	conn = psycopg2.connect(database = "d4em1jlog8luec", user = "xprroouagkusrk",
                            password = "f4cc19c667c530477e935a62d84ebe50d4235262f6134590ba3ca53b53c3e4d2",
                            host = "ec2-184-72-238-22.compute-1.amazonaws.com", port = "5432")
	cur = conn.cursor()
	cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (77.64562511, 12.87829352)")
	cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (77.64540342, 12.87832982)")
	cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (77.64391553, 12.87897244)")
	cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (77.64359296, 12.87916801)")
	cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (77.64339224, 12.87928266)")
	cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (77.64189524, 12.88110895)")
	conn.commit()

#insert locations into database
def insert_location(loc):

    conn = psycopg2.connect(database = "d4em1jlog8luec", user = "xprroouagkusrk",
                            password = "f4cc19c667c530477e935a62d84ebe50d4235262f6134590ba3ca53b53c3e4d2",
                            host = "ec2-184-72-238-22.compute-1.amazonaws.com", port = "5432")
    cur = conn.cursor()

    l1 = loc[1]
    l2 =loc[0]
    print(l1,l2)
    cur.execute("INSERT INTO public.location(latitude, longitude) VALUES (" + str(l1) + "," + str(l2) + ")")
    conn.commit()


#clear database
def clear():
    conn = psycopg2.connect(database = "d4em1jlog8luec", user = "xprroouagkusrk",
                            password = "f4cc19c667c530477e935a62d84ebe50d4235262f6134590ba3ca53b53c3e4d2",
                            host = "ec2-184-72-238-22.compute-1.amazonaws.com", port = "5432")
    cur = conn.cursor()
    cur.execute("DELETE FROM public.location")
    conn.commit()

# to collect data from database
def myrecords():
    conn = psycopg2.connect(database = "d4em1jlog8luec", user = "xprroouagkusrk",
                            password = "f4cc19c667c530477e935a62d84ebe50d4235262f6134590ba3ca53b53c3e4d2",
                            host = "ec2-184-72-238-22.compute-1.amazonaws.com", port = "5432")
    cur = conn.cursor()
    data = {}
    data_json =[]
    cur.execute("SELECT latitude, longitude from location")
    rows = cur.fetchall()
    for row in rows:
    	data['latitude'] = row[0]
    	data['longitude'] = row[1]
    	data_json.append(json.loads(json.dumps(data)))
    return data_json

