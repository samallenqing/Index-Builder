# Index-Builder

This is an application to build reverse and forward Ads index.

## Operating Requirements
- **RabbitMQ** Decouple micro-services.
- **MySQL** Database.


# Get Start

# Important Note
For test purpose, rawQuery.txt file only contains two queries or the running time will be very long. If you want to try
the full procedure, update the 'adsDataFilePath' to 'fullQuery.txt' in config.properties file.


#### OR APPLICATION WILL FAIL TO CREATE TABLE!!!

## Step 1 - Create a directory folder and download files from github

Use the following cmd in terminal to create the requests files. Here use examples "test".
```
mkdir test
cd test
git clone https://github.com/samallenqing/Index-Builder.git

```

## Step 2 - Start RabbitMQ on local PC

Go to your RabbitMQ directory and use the following cmd to run RabbitMQ on local PC

```
rabbitMQ-server

```

## Step 3 - Create some feeds by running feeder class

By processing this step, application will send some query messages via RabbitMQ.

Open any browser and go to page http://localhost:15672, with user name: guest and password: guest to login.

By sending some feeds message, you will notice information streaming in 'q_feeds' queue.


## Step 4 - Run CrawlerMain class

By processing this step, application will consume the queries from RabbitMQ and build Ad's index.

## Step 5 - Check fetching data from MySQL
You can check the data has benn fetched by the crawler and stored in MYSQL after step 4 accomplished.

You can either use the following cmd or MySQLWorkbench to check data.

eg:

```
select * from ad;

```



## Authors

* **Qin Qing** - *Initial work* - [Sam Qing](https://github.com/samallenqing)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
