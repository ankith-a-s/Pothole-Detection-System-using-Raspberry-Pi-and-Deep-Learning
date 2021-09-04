import os
import time
import time
import threading
import requests 
from gps import *
from time import *
from sense_hat import SenseHat

gpsd = None 
sense = SenseHat()
count = 0 
c = 0 
ax = [None] * 10
ay = [None] * 10
az = [None] * 10
gx = [None] * 10
gy = [None] * 10
gz = [None] * 10
lat = 0 
lon = 0
init_speed = 0
ti = time.time()
red = (255, 0, 0) 
os.system('clear') 
value = []
final_data = None
 
class GpsPoller(threading.Thread):
  def __init__(self):
    threading.Thread.__init__(self)
    global gpsd 
    gpsd = gps(mode=WATCH_ENABLE) 
    self.current_value = None
    self.running = True 
 
  def run(self):
    global gpsd
    while gpsp.running:
      gpsd.next() 
 
def getLocation():
    gpsp = GpsPoller() 
    gpsp.start() 
    lat = gpsd.fix.latitude
    lon = gpsd.fix.longitude
 

while True:
    acceleration = sense.get_accelerometer_raw()
    gyroscope = sense.get_gyroscope_raw()
    x = acceleration['x']
    y = acceleration['y']
    z = acceleration['z']
    x_g = gyroscope['x']
    y_g = gyroscope['y']
    z_g = gyroscope['z']
    x = abs(x)
    y = abs(y)
    z = abs(z)
    x_g = abs(x_g)
    y_g = abs(y_g)
    z_g = abs(z_g)

    if x > 1 or y > 1 or z > 1:
        sense.show_letter("!", red)
        curx = 0
        cury = 0
        curz = 0
        curgx = 0
        curgy = 0
        curgz = 0
        if count<10 :
            ax[count] = x
            ay[count] = y
            az[count] = z
            gx[count] = x_g
            gy[count] = y_g
            gz[count] = z_g
            count+=1
        elif count == 10 :
            for j in range(0,10):
                curx += ax[j]
                cury += ay[j]
                curz += az[j]
                curgx += gx[j]
                curgy += gy[j]
                curgz += gz[j]
        curx /= 10
        cury /= 10
        curz /= 10
        curgx /= 10
        curgy /= 10
        curgz /= 10

        if c < 50:
            getLocation()
            new_time = time.time()
            raw = sense.get_accelerometer_raw()
            y = raw['y']
            distance = (init_speed + 0.5*y*((ti - time.time())*(ti - time.time()))*3.2808)
            speed = distance / new_time
            t1 = str(int(round(time.time() * 1000))) + "," + str(curx) + "," + str(cury) + "," + str(curz) + "," + str(curgx) + "," + str(curgy) + "," + str(curgz) + "," + str(lat) + "," + str(lon) + "," + str(speed)
            value.append(t1)
            c+=1
        
        elif c == 50: 
            c = 0
            sendData()
            value.clear()
        count = 0

    else:
        sense.clear()
    



def sendData():
    final_data = None
    url = "https://pure-island-91062.herokuapp.com/data"
    for i in range(0,50):
        final_data += value[i]
    try:
        r = requests.get(url, headers=headers , data = final_data)
    except requests.exceptions.RequestException as e:  
        print e
        sys.exit(1)    

