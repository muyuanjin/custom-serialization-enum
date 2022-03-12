# custom-serialization-enum
可自由定制枚举类在json、mybaits，controller中序列化的方式
## 使用方法：
```java
@CustomSerializationEnum(myBatis = CustomSerializationEnum.Type.ID,
json = CustomSerializationEnum.Type.NAME,requestParam = CustomSerializationEnum.Type.NAME)
public enum Gender implements EnumSerialize<Gender> {
    MALE("男", 0),
    FEMALE("女", 1),
    UNKNOWN("未知", 2);

    private final String name;
    private final int id;

    Gender(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getSerializationName() {
        return name;
    }

    @Override
    public Integer getSerializationId() {
        return id;
    }
}

```
文章地址：

# [通过注解一次搞定枚举类在spring中的3种序列化的方式，再也不用头疼状态是用int、String还是enum了](https://juejin.cn/post/7074134013363322911)