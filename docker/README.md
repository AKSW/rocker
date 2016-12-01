## build the image
sudo docker build -t rocker-demo .

## run the image



sudo docker run -d --name rocker-demo -p 9999:8080  rocker-demo




## debug the image
sudo docker exec -it rocker-demo /bin/bash
