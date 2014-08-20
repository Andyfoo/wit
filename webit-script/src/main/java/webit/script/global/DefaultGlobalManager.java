// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.script.global;

import java.util.HashMap;
import java.util.Map;
import webit.script.Engine;
import webit.script.Initable;
import webit.script.util.ClassEntry;
import webit.script.lang.Bag;

/**
 *
 * @author zqq90
 */
public class DefaultGlobalManager implements GlobalManager, Initable {

    private final Map<String, Object> constMap;
    private final Map<String, Object> driftedGlobalMap;
    private final Map<String, Integer> globalIndexer;
    private Object[] globalContext;

    //settings
    private ClassEntry[] registers;

    public DefaultGlobalManager() {
        this.constMap = new HashMap<String, Object>();
        this.driftedGlobalMap = new HashMap<String, Object>();
        this.globalIndexer = new HashMap<String, Integer>();
    }

    public void init(Engine engine) {
        if (registers != null) {
            try {
                for (int i = 0, len = registers.length; i < len; i++) {
                    ((GlobalRegister) engine.getComponent(registers[i]))
                            .regist(this);
                    this.commit();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void commit() {
        if (this.driftedGlobalMap.isEmpty()) {
            return;
        }
        final int oldSize;
        final Object[] oldGlobalContext = this.globalContext;
        oldSize = oldGlobalContext != null ? oldGlobalContext.length : 0;

        final Object[] newGlobalContext = this.globalContext
                = new Object[oldSize + this.driftedGlobalMap.size()];

        if (oldSize > 0) {
            //Copy old data
            System.arraycopy(oldGlobalContext, 0, newGlobalContext, 0, oldSize);
        }

        int i = oldSize;
        for (Map.Entry<String, Object> entry : this.driftedGlobalMap.entrySet()) {
            newGlobalContext[i] = entry.getValue();
            this.globalIndexer.put(entry.getKey(), i);
            i++;
        }
        this.driftedGlobalMap.clear();
    }

    public void setConst(String key, Object value) {
        this.constMap.put(key, value);
    }

    private void setGlobal(String key, Object value) {
        int index;
        if ((index = this.getGlobalIndex(key)) >= 0) {
            this.setGlobal(index, value);
        } else {
            this.driftedGlobalMap.put(key, value);
        }
    }

    public int getGlobalIndex(String name) {
        Integer index;
        return (index = globalIndexer.get(name)) != null ? index : -1;
    }

    private Object getGlobal(String key) {
        int index;
        if ((index = this.getGlobalIndex(key)) >= 0) {
            return this.getGlobal(index);
        } else {
            return this.driftedGlobalMap.get(key);
        }
    }

    public Object getGlobal(int index) {
        return globalContext[index];
    }

    public void setGlobal(int index, Object value) {
        this.globalContext[index] = value;
    }

    public boolean hasConst(String name) {
        return this.constMap.containsKey(name);
    }

    public Object getConst(String name) {
        return this.constMap.get(name);
    }

    public void setRegisters(ClassEntry[] registers) {
        this.registers = registers;
    }

    public Bag getConstBag() {
        return new Bag() {

            public Object get(Object key) {
                return constMap.get(key);
            }

            public void set(Object key, Object value) {
                constMap.put(String.valueOf(key), value);
            }
        };
    }

    public Bag getGlobalBag() {
        return new Bag() {

            public Object get(Object key) {
                return getGlobal(String.valueOf(key));
            }

            public void set(Object key, Object value) {
                setGlobal(String.valueOf(key), value);
            }
        };
    }

}
