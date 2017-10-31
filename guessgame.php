<!DOCTYPE html>
<html>
<head>
<title>Guessing Game for Sam Qing</title>
</head>
<body>
<h1>Welcome to my guessing game</h1>
<p>
  <?php
  if (! isset($_GET['guess'])){
    echo "Missing guess parameter";
  } elseif (strlen($_GET['guess'] < 1)) {
    echo "Your guess in too short";
  } elseif (! is_numeric($_GET['guess'])) {
    echo "Your guess is not a numbre";
  } elseif ($_GET['guess']< 40) {
    echo "Your guess is too low";
  } elseif ($_GET['guess'] > 40) {
    echo "Your guess is too high";
  } else {
    echo "Congratulations - You are right";
  }
   ?>

</p>
</body>
</html>
