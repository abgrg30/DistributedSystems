#!bin/bash
docker stop catserver
docker stop catclient
docker stop dvc
docker rm catserver
docker rm catclient
docker rm dvc
docker build -t abhinavgarg/dvc:1.0 ./voldir
cd scdir
cp ../catserver.py .
cp ../catclient.py .
docker build -t abhinavgarg/scimage:1.0 .
cd ..
docker run -id --name dvc abhinavgarg/dvc:1.0
docker run -id --volumes-from dvc --name catserver abhinavgarg/scimage:1.0 python /code/catserver.py /data/string.txt 8000
docker run -id --volumes-from dvc --name catclient --link catserver abhinavgarg/scimage:1.0 python /code/catclient.py /data/string.txt 8000
docker logs -f catclient
