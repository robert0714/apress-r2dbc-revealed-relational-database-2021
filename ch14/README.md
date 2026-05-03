# Chapter 14 - Transaction Management Samples.
Structure:
```bash
com/example/
├── App.java                     ← 精簡編排層 (~55 行)
├── ConnectionProvider.java      ← 連線生命週期管理
└── sample/
    ├── TransactionSample.java           ← 介面 (OCP/DIP)
    ├── ResetTaskTableSample.java        ← reset
    ├── CommitTransactionSample.java     ← commit
    ├── RollbackTransactionSample.java   ← rollback
    ├── ImperativeTransactionSample.java ← imperative (blocking 風格教學對比)
    ├── DeclarativeTransactionSample.java← declarative (純 reactive chain)
    └── SavePointSample.java             ← savepoint
```
## 關鍵改善
| 修復                                 | 做法                                                     |
|------------------------------------|--------------------------------------------------------|
| Race condition                     | 所有 sample 返回 Mono<Void>，main 用 .block() 等待完成後才關閉連線     |
| 不再需要 Thread.currentThread().join() | 因為所有操作都被正確 await                                       |
| 連線一定被關閉                            | finally block 確保                                       |
| 切換示範                               | 用 command-line args 取代註解切換：java com.example.App commit |

## 執行方式
```bash
# 預設執行 reset
mvn exec:java -Dexec.mainClass=com.example.App

# Listing 14-1: Reset the tasks table to a known state.
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="reset"

# Listings 14-4 and 14-5: Imperative transaction commit example. Demonstrates beginTransaction → execute → commitTransaction → verify.
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="commit"

# Listing 14-7: Demonstrates transaction rollback. Deletes data within a transaction, then rolls back, verifying data is unchanged.
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="rollback"

# Listing 14-9 (declarative style): Full reactive chain with error handling via onErrorResume. No blocking calls — pure reactive pipeline.
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="declarative"

# Listing 14-9 (imperative style): Demonstrates commit on success, rollback on exception. Intentionally uses blocking (.block()) style for pedagogical comparison with declarative approach.
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="imperative"

# Listings 14-12 and 14-13: Demonstrates savepoint creation, optional rollback to savepoint, and commit.
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="savepoint"
```
