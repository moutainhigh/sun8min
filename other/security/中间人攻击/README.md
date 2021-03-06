## 中间人攻击
###### [wiki链接](https://zh.wikipedia.org/wiki/中间人攻击)
- 中间人攻击（英语：Man-in-the-middle attack，缩写：MITM）
- 在密码学和计算机安全领域中，是指攻击者与通讯的两端分别创建独立的联系，
并交换其所收到的数据，使通讯的两端认为他们正在通过一个私密的连接与对方直接对话，
但事实上整个会话都被攻击者完全控制。
- 在中间人攻击中，攻击者可以拦截通讯双方的通话并插入新的内容。

一个中间人攻击能成功的前提条件是攻击者能将自己伪装成每一个参与会话的终端，
并且不被其他终端识破。中间人攻击是一个（缺乏）相互认证的攻击。
大多数的加密协议都专门加入了一些特殊的认证方法以阻止中间人攻击。
例如，SSL协议可以验证参与通讯的一方或双方使用的证书是否是由权威的受信任的数字证书认证机构颁发，
并且能执行双向身份认证。

###### example:
假设爱丽丝（Alice）希望与鲍伯（Bob）通信。
同时，马洛里（Mallory）希望拦截窃会话以进行窃听并可能在某些时候传送给鲍伯一个虚假的消息。

首先，爱丽丝会向鲍勃索取他的公钥。
如果Bob将他的公钥发送给Alice，并且此时马洛里能够拦截到这个公钥，就可以实施中间人攻击。
马洛里发送给爱丽丝一个伪造的消息，声称自己是鲍伯，并且附上了马洛里自己的公钥（而不是鲍伯的）。

爱丽丝收到公钥后相信这个公钥是鲍伯的，于是爱丽丝将她的消息用马洛里的公钥（爱丽丝以为是鲍伯的）加密，
并将加密后的消息回给鲍伯。马洛里再次截获爱丽丝回给鲍伯的消息，并使用马洛里自己的私钥对消息进行解密，
如果马洛里愿意，她也可以对消息进行修改，然后马洛里使用鲍伯原先发给爱丽丝的公钥对消息再次加密。
当鲍伯收到新加密后的消息时，他会相信这是从爱丽丝那里发来的消息。

#### 主要危害：
1. 与服务器的通信被第三方解密、查看、修改。

#### 主要解决办法：
1. 针对web：使用https，用受信任的证书颁发机构（CA）颁发的证书


