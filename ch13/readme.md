 
# Check the current status of the stack system.
```sql
show engine innodb status;
```
# Find transactions (unfinished transactions) that exist in the InnoDB transaction system.
```sql
SELECT * FROM information_schema.innodb_trx;

SELECT 
    trx_id, 
    trx_state, 
    trx_started, 
    trx_mysql_thread_id, 
    trx_query 
FROM 
    information_schema.innodb_trx
WHERE 
    trx_started < NOW() - INTERVAL 10 SECOND;

SELECT
    t.trx_id,
    t.trx_state,
    t.trx_started,
    p.ID AS process_id,
    p.USER,
    p.HOST,
    p.DB,
    p.COMMAND,
    p.TIME,
    p.STATE,
    t.trx_query
FROM
    information_schema.innodb_trx t
JOIN
    information_schema.processlist p ON t.trx_mysql_thread_id = p.ID;

SELECT * FROM information_schema.innodb_lock_waits; 
```
# MySQL session
```
SHOW PROCESSLIST;
```