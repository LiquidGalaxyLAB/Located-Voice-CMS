<p align="center">
  <img alt="Located Voice CMS" src="https://github.com/vedantkingh/Located-Voice-CMS/blob/master/assets/ReadMeContent/lvc-logo.png?raw=true" height="250px">
</p>  
<h1 align="center"> Located Voice CMS </h1>

## Table of Contents

- **[About Located Voice CMS](#about-located-voice-cms)**
- **[App Screenshots](#app-screenshots)**
- **[Running the APK](#running-the-apk)**
- **[AI Server Guide](#ai-server-guide)**
- **[Previous Versions](#previous-versions)**
- **[Contributing](#contributing)**


## About Located Voice CMS

* Located Voice CMS was developed as a **Google Summer of Code 2023 project with the Liquid Galaxy Project**. Details can be viewed [here](https://summerofcode.withgoogle.com/programs/2023/projects/n3hWZbED).
* Located Voice CMS is an Android app that lets the user connect to a Liquid Galaxy system and send POIs and Tours. At the same time, the app also lets to create, update and delete POIs, Tours, and Categories. Use various tools to control your Liquid Galaxy System.
Access the latest features:
1. **Artificial Intelligence Voice:** Listen to pre-recorded AI-generated voices describing the POIs stored on the cloud.
2. **Artificial Intelligence Context:** Connect to the AI server and generate your own real-time audio via Suno's Bark Generative Audio AI Model. To do this, just run the following commands on your AI server:
    1. **Pull the docker image:**
       ```bash
    	docker pull vedantkingh/bark2
       ```
    2. **To run on CPU:**
       ```bash
  		docker run vedantkingh/bark2
       ```
  
       **To run on GPU:**
       ```bash
  		docker run --gpus all vedantkingh/bark2 
       ```
4. **Category Sounds:** Feel the POIs ambience around you with immersive sounds to your categories. You can also add, edit and delete the category sounds.
5. **Context Sensing:** With the Nearby Places feature, you can now generate nearby places out of thin air to visit around a certain POI. Best experienced when coupled with Artificial Intelligence Context.

## App Screenshots

<div style="display: flex; justify-content: space-between; flex-wrap: wrap;">
    <img src="https://github.com/vedantkingh/Located-Voice-CMS/assets/123883929/f7f5374b-3212-4a85-be3f-ee314c311f1a" alt="Screenshot 1" style="width: 49%; margin-right: 1%; margin-bottom: 10px;">
    <img src="https://github.com/vedantkingh/Located-Voice-CMS/assets/123883929/af97b383-e6ad-43aa-a8fd-643d5c81d2f2" alt="Screenshot 2" style="width: 49%; margin-left: 1%; margin-bottom: 10px;">
    <img src="https://github.com/vedantkingh/Located-Voice-CMS/assets/123883929/c711b3dc-1f90-4868-a46f-ecc3f67091f0" alt="Screenshot 3" style="width: 49%; margin-right: 1%; margin-bottom: 10px;">
    <img src="https://github.com/vedantkingh/Located-Voice-CMS/assets/123883929/99f0087b-d922-44f2-b8fc-fc4693043535" alt="Screenshot 4" style="width: 49%; margin-left: 1%; margin-bottom: 10px;">
    <img src="https://github.com/vedantkingh/Located-Voice-CMS/assets/123883929/4404aba5-ddf2-40a8-ab41-9b80d79da639" alt="Screenshot 5" style="width: 49%; margin-right: 1%; margin-bottom: 10px;">
</div>


## Running the APK

### Prerequisites

* Android device. Preferrably a 10-inch Android Tablet

### Steps:

* Download the apk file from [this repository](https://github.com/vedantkingh/Located-Voice-CMS/raw/master/app/release/app-release.apk) or the [Google Play Store]().
* To connect to the Liquid Galaxy and the AI Server, tap on menu icon and go to Administration Tools > Settings then fill up the details of your Liquid Galaxy Rig (LG Server IP, LG Server ID, LG Server Password and the number of machines) and the AI Server (AI Server IP and the Port on which the docker is running).
* Now simply explore the application, send a wide variety of KML Data to the LG and listen to immersive audio via our Cloud as well as your local AI Server. 

## AI Server Guide

- The AI server is used for real-time AI audio generation. It runs the [Bark](https://github.com/suno-ai/bark) created model by [Suno AI](https://www.suno.ai/) in your AI server.
- Running your own AI server for Located Voice CMS is fairly simple. You can simply run the API in a dockerized container using the [Docker Image](https://hub.docker.com/repository/docker/vedantkingh/bark2/general). To do this, just run the following commands on your AI server:
    1. **Pull the docker image:**
       ```bash
       docker pull vedantkingh/bark2
       ```
    2. **To run on CPU:**
       ```bash
  	   docker run vedantkingh/bark2
       ```
  
       **To run on GPU:**
       ```bash
       docker run --gpus all vedantkingh/bark2 
       ```
- If you want to run the API without docker or dig deep into the API which is running the model. It is available [at this repository](https://github.com/vedantkingh/bark).
    1. **Clone the repository:**
         ```bash
  		git clone https://github.com/vedantkingh/bark.git
         ```
    2. **Install dependencies:**
       ```bash
  	   sudo apt-get update && sudo apt-get upgrade -y
       sudo apt-get install -y python3-dev python3-pip build-essential sox libsox-fmt-mp3
       sudo apt-get install -y nvidia-cudnn
       pip install --no-cache-dir -r requirements.txt
       ```
    3. Start the Flask API on the server:
        ```bash
        python app.py
        ```
    4. Send a POST request to the /synthesize endpoint with the desired text as JSON payload. For example:
        ```bash
        POST /synthesize
        Content-Type: application/json
        
        {
            "text": "Hello, my name is Suno. And, uh — and I like pizza. [laughs] But I also have other interests such as playing tic tac toe."
        }
        ```
    This will generate the voice audio corresponding to the provided text.

## Previous Versions
An Android app that lets the user connect to one Liquid Galaxy system and send POIs and Tours. At the same time, the app also lets to create, update and delete POIs, Tours and Categories.

This project is a GSOC 2023 project that continues a previous project.
This GSoC 2023 project contains the following subprojects for each one of them a commits link is provided

* Located Voice CMS: https://github.com/vedantkingh/Located-Voice-CMS/commits/master?author=vedantkingh
* Liquid Galaxy Controller : https://github.com/LiquidGalaxyLAB/Liquid-Galaxy-POIs-Controller/commits/master?author=navijo
* BYOP : https://github.com/LiquidGalaxyLAB/BYOP/commits/master?author=navijo
* PhysicalWebLGPoiReader : https://github.com/LiquidGalaxyLAB/PhysicalWebLGPoiReader/commits/master?author=navijo

## Contributing 

Fill up issues, bugs or feature requests in our issue tracker. Please be very descriptive and clear so it is easier to help you.
If you want to contribute to this project you can open a pull request at time you like. 
