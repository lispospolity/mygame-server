To run this code you need mariadb with databases:

1. users:
    a) CREATE TABLE userspw (
    username VARCHAR(50) NOT NULL,
    pwd_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (username),
    UNIQUE (username)
    );
    b) CREATE TABLE sessions (
    username VARCHAR(50) NOT NULL,
    token VARCHAR(50) NOT NULL,
    time BIGINT(20) NOT NULL,
    PRIMARY KEY (username)
    );
2. userstates:
    b) CREATE TABLE userloc (
    username VARCHAR(50) NOT NULL,
    x INT(11) NOT NULL,
    y INT(11) NOT NULL,
    PRIMARY KEY (username)
    );

You will also need a .env file with the DB class that includes

1. jdbc:mariadb://localhost:3306/users
2. your user name for example root (i make my own with only the priveleges it needs)
3. user password

5. jdbc:mariadb://localhost:3306/userstates

Last thing you might need is hosting the gamejs files from an apache2 or IIS service
