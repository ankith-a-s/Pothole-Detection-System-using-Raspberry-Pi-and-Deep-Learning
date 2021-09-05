from bottle import run, get, post, request, delete, Bottle
#import database
import database
import model

app = Bottle()

@app.route('/location')
def getAll():
	#insert_db()
	return {'location' : database.myrecords()}

	#database.clear()

@app.route('/clear/delete/db')
def clear_DB():
	database.clear()



@app.route('/data',method='POST')
def process():	
	model.show(request.json)
	model.predictPotholes(request.json)
	#print(type(request.json))
	#return str




#following for normal bottle app
#run(host = '0.0.0.0', reloader=True, server = 'gunicorn', debug=True, workers=4)

# to run a post request use following
# r = requests.post('http://localhost:8080/animal', json = dt)
