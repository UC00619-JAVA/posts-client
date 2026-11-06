UC00619 — Desenvolvimento em Java — Sessão 12
Exercício 4 — Publicações do JSONPlaceholder
Projeto: posts-client · Spring Boot 4.1.2 · Java 21 · Maven · Spring Web + Lombok
================================================================

PEDIDOS                                       RESULTADO ESPERADO
GET  http://localhost:8080/api/posts?userId=1   200, lista com 10 posts (ids 1 a 10)
GET  http://localhost:8080/api/posts/1          200 {"id":1,"title":"sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
                                                     "body":"quia et suscipit ...","commentCount":5}
GET  http://localhost:8080/api/posts/999        404 {"status":404,"code":"NOT_FOUND","message":"Resource not found in JSONPlaceholder"}
POST http://localhost:8080/api/posts  (Body → raw → JSON)
     {"userId":1,"title":"My first post","body":"Hello from Spring Boot"}
                                                201, Location: /api/posts/101
                                                {"userId":1,"id":101,"title":"My first post","body":"Hello from Spring Boot"}
                                                (o JSONPlaceholder responde sempre 101 e não guarda nada)
Extra:
GET  http://localhost:8080/api/posts/1/commenters
     200 ["Eliseo@gardner.biz","Jayne_Kuhic@sydney.com","Nikita@garfield.biz","Lew@alysha.tv","Hayden@althea.biz"]

