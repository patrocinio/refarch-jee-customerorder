
-------------------------------------------------- CREATE TABLES --

CREATE TABLE CUSTOMER  (

		  CUSTOMER_ID INTEGER NOT NULL ,

		  USERNAME VARCHAR(30) NOT NULL,

		  OPEN_ORDER INTEGER , 

		  NAME VARCHAR(30) NOT NULL , 

		  BUSINESS_VOLUME_DISCOUNT CHAR(1) DEFAULT 'N', 

		  BUSINESS_PARTNER CHAR(1) DEFAULT 'N', 

		  BUSINESS_DESCRIPTION CLOB(12582912) , 

		  RESIDENTIAL_HOUSEHOLD_SIZE SMALLINT , 

		  RESIDENTIAL_FREQUENT_CUSTOMER CHAR(1) DEFAULT 'Y',
		  TYPE VARCHAR(11) NOT NULL,
		  ADDRESSLINE1 VARCHAR(50),
		  ADDRESSLINE2 VARCHAR(50),
		  CITY VARCHAR(50),
		  COUNTRY VARCHAR(50),
		  STATE VARCHAR(30),
		  ZIP VARCHAR (10)
);

CREATE TABLE FAVORITES
(
	CUSTOMER_ID INTEGER NOT NULL ,
	favorite VARCHAR(30)
);
		  
CREATE TABLE SHIPPING_ADDRESS
( 			
	CUSTOMER_ID INTEGER NOT NULL ,
	ADDRESSLINE1 VARCHAR(50),
	ADDRESSLINE2 VARCHAR(50),
	CITY VARCHAR(50),
	COUNTRY VARCHAR(50),
	STATE VARCHAR(30),
	ZIP VARCHAR (10)
);



CREATE TABLE CREDIT_INFO
(
	NUMBER VARCHAR (25) NOT NULL PRIMARY KEY,
	CUSTOMER_ID INTEGER NOT NULL ,
	NAME VARCHAR(50),
	COMPANY VARCHAR(25),
	SECNUMBER VARCHAR(10),
	EXPIRATIONMONTH INTEGER,
	EXPIRATIONYEAR INTEGER,
	ADDRESSLINE1 VARCHAR(50),
	ADDRESSLINE2 VARCHAR(50),
	CITY VARCHAR(50),
	COUNTRY VARCHAR(50),
	STATE VARCHAR(30),
	ZIP VARCHAR (10)
);





CREATE TABLE LINE_ITEM  (

		  ORDER_ID INTEGER NOT NULL , 

		  PRODUCT_ID INTEGER NOT NULL , 

		  QUANTITY BIGINT NOT NULL , 

		  AMOUNT DECIMAL(14,2) NOT NULL );

CREATE TABLE ORDERS  (

		  ORDER_ID INTEGER NOT NULL AUTO_INCREMENT, 

		  CUSTOMER_ID INTEGER NOT NULL , 

		  STATUS VARCHAR(9) NOT NULL , 

		  VERSION INTEGER NOT NULL DEFAULT 0,
		
	          SUBMIT_TIME TIMESTAMP ,		

		  TOTAL DECIMAL(14,2) NOT NULL );
		  
CREATE TABLE ORDER_REC 
(
	ORDER_ID INTEGER NOT NULL PRIMARY KEY,
	NUMBER VARCHAR (25) NOT NULL,
	LINE1 VARCHAR(50),
	LINE2 VARCHAR(50),
	CITY VARCHAR(50),
	COUNTRY VARCHAR(50),
	STATE VARCHAR(30),
	ZIP VARCHAR (10)
);

CREATE TABLE PRODUCT  (

		  PRODUCT_ID INTEGER NOT NULL, 

		  PRICE DECIMAL(14,2) NOT NULL , 

		  NAME VARCHAR(50) NOT NULL,
		  DESCRIPTION VARCHAR(256),
		  IMAGE VARCHAR(200));

CREATE TABLE CATEGORY (
	CAT_ID INTEGER AUTO_INCREMENT,
	CAT_NAME VARCHAR (100) NOT NULL,
	PARENT_CAT INTEGER
);

CREATE TABLE PROD_CAT (
	PC_ID INTEGER AUTO_INCREMENT,
	CAT_ID INTEGER,
	PRODUCT_ID INTEGER
);

CREATE TABLE CONTACT_NUMBERS
(
     CUSTOMER_ID INTEGER NOT NULL,
     TYPE VARCHAR(25) NOT NULL PRIMARY KEY,
     PHONE VARCHAR(15) NOT NULL
);

------------------------------------------------ DEFINE PRIMARY/FOREIGN KEYS --




ALTER TABLE CATEGORY ADD CONSTRAINT CAT_KEY PRIMARY KEY (CAT_ID);

ALTER TABLE PROD_CAT ADD CONSTRAINT PC_KEY PRIMARY KEY (PC_ID);

ALTER TABLE CUSTOMER ADD CONSTRAINT CUSTOMER_KEY PRIMARY KEY (CUSTOMER_ID);

ALTER TABLE LINE_ITEM 

	ADD CONSTRAINT LINE_ITEM_KEY PRIMARY KEY

		(ORDER_ID,

		 PRODUCT_ID);

ALTER TABLE ORDERS 

	ADD CONSTRAINT ORDER_KEY PRIMARY KEY

		(ORDER_ID);

ALTER TABLE PRODUCT 

	ADD CONSTRAINT PRODUCT_KEY PRIMARY KEY

		(PRODUCT_ID);

ALTER TABLE CUSTOMER ADD CONSTRAINT ORDER_FOREIGN1 FOREIGN KEY(OPEN_ORDER) REFERENCES ORDERS(ORDER_ID);

ALTER TABLE LINE_ITEM ADD CONSTRAINT PRODUCT_FOREIGN FOREIGN KEY(PRODUCT_ID) REFERENCES PRODUCT(PRODUCT_ID);

ALTER TABLE LINE_ITEM ADD CONSTRAINT ORDER_FOREIGN2 FOREIGN KEY(ORDER_ID) REFERENCES ORDERS(ORDER_ID);

ALTER TABLE ORDERS 

	ADD CONSTRAINT CUSTOMER_FOREIGN FOREIGN KEY

		(CUSTOMER_ID)

	REFERENCES CUSTOMER

		(CUSTOMER_ID);
		
ALTER TABLE ORDER_REC

	ADD CONSTRAINT ORDER_FK FOREIGN KEY

		(ORDER_ID)

	REFERENCES ORDERS

		(ORDER_ID);
ALTER TABLE ORDER_REC

	ADD CONSTRAINT ORDER_CID_FK FOREIGN KEY

		(NUMBER )

	REFERENCES CREDIT_INFO

		(NUMBER);

ALTER TABLE CATEGORY 

	ADD CONSTRAINT CAT_PAR FOREIGN KEY

		(PARENT_CAT)

	REFERENCES CATEGORY

		(CAT_ID);

ALTER TABLE PROD_CAT 

	ADD CONSTRAINT PC_PROD FOREIGN KEY

		(PRODUCT_ID)

	REFERENCES PRODUCT

		(PRODUCT_ID);

ALTER TABLE PROD_CAT 

	ADD CONSTRAINT PC_CAT FOREIGN KEY

		(CAT_ID)

	REFERENCES CATEGORY

		(CAT_ID);
		
		ALTER TABLE CONTACT_NUMBERS ADD CONSTRAINT CN FOREIGN KEY(CUSTOMER_ID) REFERENCES CUSTOMER(CUSTOMER_ID);

---------------------------------------------- CONSTRAINTS --




ALTER TABLE ORDERS 

	ADD CONSTRAINT STATUS_ENUMERATION CHECK 

		(STATUS IN ('OPEN', 'SUBMITTED','CLOSED','SHIPPED'));
