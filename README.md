shakey
======
[![Build Status](https://secure.travis-ci.org/jcai/shakey.svg?branch=master)](https://travis-ci.org/jcai/shakey)

本项目为美股投资项目，目的是为大家赚钱。。。。。

股市有风险，投资需谨慎

目标(v1.0)
=========
* 自动监控程序天量出现时刻
* 能在天量出现时刻分析，当前股价变化情况，譬如：上升还是下跌，以及幅度
* 能够根据出现时刻，以及变化情况，能自动下单，并且监控订单情况，能够及时止损或者获利


技术
=========
* scala        编程语言 OOP + FP
* tapestry-ioc 使用ioc框架，管理程序中的对象
* metrics      对实时订单交易量进行度量，及时给出天量信息
* maven        构建工具，能够支持进行持续集成
* nsis         安装脚本，能够包装java程序，制作windows下的安装包



