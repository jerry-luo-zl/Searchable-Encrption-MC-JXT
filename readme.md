# 多用户场景下支持Join查询的对称可搜索加密方案

本项目设计提出了首个多用户场景下支持Join查询的对称可搜索加密协议，并称之为MC-JXT（Multi-Client Join Cross-Tags），协议在单用户对称可搜索加密协议JXT（Join Cross-Tags）的基础上设计而来，采取其将Join查询拆分为多个非Join子查询的思路，既高效支持Join查询，又支持多用户数据共享。针对多用户对称可搜索加密协议设计中搜索令牌分发和服务端搜索令牌验证这两个核心问题，协议分别采用子令牌拆分和同态签名的解决方案。在数据提供者接收检索者请求并给检索者分发搜索令牌时，协议通过子令牌拆分技术，将令牌的一部分交给检索者本地生成，降低了搜索令牌分发的通信开销。同时协议采用基于一次性盲化因子的同态签名方案，使得协议在保证高效令牌分发的同时，实现服务端对检索者发送的搜索令牌的合法性验证。

+ MC-JXT-Proj为协议实现代码

+ JXT协议论文链接: Efficient Searchable Symmetric Encryption for Join Queries

  https://eprint.iacr.org/2021/1471

