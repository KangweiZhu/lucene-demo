```bash
archlinux-java status
```

你要实现的接口是一个基于 Lucene 的搜索服务，用户可以访问：
```
GET /search?pickup=jfk&dropoff=times square
```
后端使用 Lucene 索引搜索，并返回匹配的打车记录（pickup、dropoff、datetime、total_amount）。

✅ 步骤概览
建一个返回结构类：SearchResult
写一个 Lucene 查询服务类：LuceneSearchService
写一个 REST 控制器：SearchController
（可选）写一个初始化方法，在 Spring Boot 启动时构建索引（用于本地演示）


```bash
sudo usermod -aG docker $USER
```
```bash
docker run --name mysql-taxi \
  -e MYSQL_ROOT_PASSWORD=root \
  -v ~/docker/mysql-taxi-data:/var/lib/mysql \
  -p 3306:3306 \
  -d mysql:8
```

Copy the NYC dataset into MySQL
```
docker cp dataset/2023_Yellow_Taxi_Trip_Data.csv mysql-taxi:/var/lib/mysql-files/
```
sql直接照着init.sql顺序执行即可。

```
共写入文档数：37000870
```

```
GET /search?picklarup=jfk&dropoff=midtown
GET /search?pickup=central park
```