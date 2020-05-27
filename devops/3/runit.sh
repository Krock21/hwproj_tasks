sudo docker build -t hwproj_task3 .
sudo docker run -it --mount src="$(pwd)/www",target=/var/www,type=bind -p 8080:8080 hwproj_task3
