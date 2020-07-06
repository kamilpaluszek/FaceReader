# FaceReader
Android application that recognizes human emotions judging by photo of face. <br> It uses server made in Flask [(server_facereader)](https://github.com/kamilpaluszek/server_facereader) to communicate and classify photos sent by this Application.
<br>You can pick your photo from gallery or take a photo by front or back camera in your mobile phone. <br><br>
Example of photo picked from gallery which is not yet classified:<br><br>
![Screenshot from FaceReader application made before the classification](https://raw.githubusercontent.com/kamilpaluszek/FaceReader/master/face1.png)
<br>
<br>After the classification made on server side:<br><br>
![Screenshot from FaceReader application made after the classification](https://raw.githubusercontent.com/kamilpaluszek/FaceReader/master/face2.png)
<br><br>The server uses CNN model of our neural network created on FER-2013 dataset. <br>It has about 66% accuracy. <br>
