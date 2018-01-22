

CREATE SEQUENCE user_ids;
CREATE TABLE users (
  id BIGINT PRIMARY KEY DEFAULT NEXTVAL('user_ids'),
  email VARCHAR(34),
  nickname VARCHAR(34),
  password VARCHAR(34));

INSERT INTO users (email,nickname, password)   VALUES ('mike@yahoo.com', 'Mike','pass');
INSERT INTO users (email,nickname, password)   VALUES ('john@aol.com', 'John','trustno1');
INSERT INTO users (email,nickname, password)   VALUES ('billy@gmail.com', 'Billy','12345');
INSERT INTO users (email,nickname, password)   VALUES ('donald@zoho.com', 'Donald','11111');
INSERT INTO users (email,nickname, password)   VALUES ('admin@icloud.com', 'Admin','999');