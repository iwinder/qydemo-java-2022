## 静态代理

- Subject：抽象角色。可以是接口，也可以是抽象类。声明真实对象（Real Subject）和代理对象(Proxy Subject)的共同接口。
- Real Subject：真实角色。定义代理角色所代表的真实实体，即最终要引用的对象。
- Proxy Subjcet：代理角色。包含真实对象的引用，负责对真实对象的调用，并在真实对象处理前后做预处理与善后工作；提供与真是对象（Real Subject）相同的接口以便任何时候都可以代替真实实体。

为保持行为的一致性，代理角色与真实角色通常会实现相同的接口。

### 优点
- 职责清晰：真是角色只需关注业务逻辑的实现。非业务逻辑的部分交给后期的代理角色即可。
- 高扩展：可以在不修改真实对象的前提下，对目标功能扩展。

### 缺点
- 代理角色的一个接口对应一种类型的对象（即代理角色与真实角色通常会实现相同的接口），如果要代理的过多，就会产生大量的类，从而导致类的急速膨胀。
- 接口添加一个方法后，所有实现类都要实现该方法，所有的代理类也需要实现该方法。增加了代码维护的复杂度。