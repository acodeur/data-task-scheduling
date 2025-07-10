package link.zzone.server.node.enums;

/**
 * @author chrischen
 * @date 2025/7/10
 * @desc NodeStateEnum
 */
public enum NodeInstanceStateEnum {

    /**
     * 节点状态枚举类
     */
    NEW(0, "未运行"),
    WAIT(1, "等待"),
    RUNNING(2, "运行"),
    SUCCESS(3, "成功"),
    FAIL(4, "失败"),
    SKIP(5, "跳过"),
    STOP(6, "停止");

    Integer code;
    String value;

    NodeInstanceStateEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
