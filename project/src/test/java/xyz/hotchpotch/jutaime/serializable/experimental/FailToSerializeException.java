package xyz.hotchpotch.jutaime.serializable.experimental;

/**
 * シリアル化に失敗したことを表す実行時例外です。<br>
 * 失敗の原因となった例外を cause として保持します。<br>
 * 
 * @author nmby
 */
public class FailToSerializeException extends RuntimeException {
    
    // ++++++++++++++++ static members ++++++++++++++++
    
    // ++++++++++++++++ instance members ++++++++++++++++
    
    public FailToSerializeException(Throwable cause) {
        super(cause);
    }
}
