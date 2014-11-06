shakey
======
[![DevOps By Rultor.com](http://www.rultor.com/b/ganshane/shakey)](http://www.rultor.com/p/ganshane/shakey)
[![Build Status](https://travis-ci.org/ganshane/shakey.svg?branch=master)](https://travis-ci.org/ganshane/shakey)

本项目为美股投资项目，目的是为大家赚钱。。。。。

股市有风险，投资需谨慎

目标(v1.0)
=========
* 自动分析股票的天量值，支持两种模式，通过日均计算和5分钟抽样计算模式.(已完成)
* 自动监控股票天量出现时刻,并且给出提示信息.(已完成)
* 能在天量出现时刻分析，当前股价变化情况，譬如：上升还是下跌，以及幅度
* 能够根据出现时刻，以及变化情况，能自动下单，并且监控订单情况，能够及时止损或者获利


技术
=========
* scala        编程语言 OOP + FP
* tapestry-ioc 使用ioc框架，管理程序中的对象
* metrics      对实时订单交易量进行度量，及时给出天量信息
* maven        构建工具，能够支持进行持续集成
* nsis         安装脚本，能够包装java程序，制作windows下的安装包

配置文件
========

配置文件中比较关键的是策略的配置

```
<shakey>
  <!-- 日志文件存放路径 -->
  <log_file>${server.home}/log/shakey.log</log_file>
  <ib_account>xxx</ib_account>
  <ib_api_host>localhost</ib_api_host>
  <ib_api_port>4001</ib_api_port>
  <!-- 监控股票列表 -->
  <stocks>VIPS,YOKU,JD,EJ,CSIQ</stocks>
  <!--
  <stocks>BABA,TSM,MPEL,HIMX,SFUN,TSL,JKS,JRJC,JD,BIDU,QIHU,CSIQ,NQ,HPJ,VIMC,YOKU,JASO,CTRP,EDU,VNET,EJ,DANG,CMCM,CHL,VIPS,BITA,KNDI,WUBA,ATHM,SINA,YY,GAME,WB,JMEI,LEJU,QUNR,ASX,XRS,CMGE,SIMO,SPIL,TOUR,MOBI,LITB,TEDU,WX,WBAI,CHU,NTES,HOLI,XNET,GOMO,NOAH,CXDC,PWRD,FENG</stocks>
  -->
  <!-- 天量获取的策略,有两种方式
  FiveMinute  从IB实时抓取5分钟数据进行计算,和后面的 top_percent 配合使用
  Day   从sina抓取每天的数据进行计算,和后面的 rate_overflow配合使用
  -->
  <volume_strategy>Day</volume_strategy>
  <rate_overflow>2</rate_overflow>
  <!--
  在5分钟计算的时候，取topN的比率
  -->
  <top_percent>0.1618</top_percent>
</shakey>
```




