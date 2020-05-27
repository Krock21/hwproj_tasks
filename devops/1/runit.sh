sudo docker build -t hwproj_task1 .
sudo docker run -it --mount src="$(pwd)/script",target=/var/script,type=bind hwproj_task1
