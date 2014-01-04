// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.script.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.script.loggers.Logger;
import webit.script.util.bean.BeanUtil;
import webit.script.util.props.Props;

/**
 * A Simple IoC. Note: <code>defineParameters</code> and
 * <code>defineParameter</code> are not thread safe
 *
 * @author zqq90
 */
public final class Petite {

    private final Map<String, Object> parameters;
    private Map<String, ParameterEntry> parameterEntrys;
    private boolean initalized = false;
    private Logger logger;

    public Petite() {
        this.parameters = new HashMap<String, Object>();
    }

    public String resolveBeanName(Class<?> type) {
        return type.getName();
    }

    public void wireBean(Object bean) {
        wireBean(resolveBeanName(bean.getClass()), bean);
    }

    public void wireBean(String beanName, final Object bean) {
        final Map<String, Object> params;
        getParametersResolver().resolve(params = new HashMap<String, Object>(), beanName);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                BeanUtil.set(bean, entry.getKey(), entry.getValue(), true);
            } catch (Exception ex) {
                if (logger != null) {
                    logger.warn("Failed to set declared property: ".concat(entry.getKey()), ex);
                }
            }
        }
    }

    public void defineParameters(Map<String, Object> parameters) {
        parameters.putAll(parameters);
        initalized = false;
    }

    public void defineParameters(Props props, Map<String, Object> parameters) {
        if (props == null) {
            props = new Props();
        }
        final Map<String, Object> extraDirectParameters;
        if (parameters != null) {
            extraDirectParameters = new HashMap<String, Object>();
            String name;
            Object value;
            int len;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                if ((name = entry.getKey()) == null) {
                    continue;
                }
                name = name.trim();
                value = entry.getValue();
                if (value instanceof String) {
                    if ((len = name.length()) > 0) {
                        if (name.charAt(len - 1) == '+') {
                            props.append(name.substring(0, len - 1).trim(), (String) value);
                        } else {
                            props.load(name, (String) value);
                        }
                    }
                } else {
                    extraDirectParameters.put(name, value);
                }
            }
        } else {
            extraDirectParameters = null;
        }
        //
        props.extractProps(this.parameters);
        if (extraDirectParameters != null && extraDirectParameters.size() > 0) {
            this.parameters.putAll(extraDirectParameters);
        }
        initalized = false;
    }

    public void defineParameter(String name, Object value) {
        parameters.put(name, value);
        initalized = false;
    }

    public Object getParameter(String name) {
        return parameters.get(name);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void initalize() {
        if (initalized == false) {
            Map<String, ParameterEntry> paramEntrys = this.parameterEntrys = new HashMap<String, ParameterEntry>();
            String key;
            int index;
            int index2;
            String entryKey;
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                key = entry.getKey();
                if ((index = key.lastIndexOf('.')) > 0
                        && (index2 = index + 1) < key.length()
                        && key.charAt(index2) != '@') {

                    paramEntrys.put(entryKey = key.substring(0, index),
                            new ParameterEntry(
                                    key.substring(index2),
                                    entry.getValue(),
                                    paramEntrys.get(entryKey)));
                }
            }
            initalized = true;
        }
    }

    private ParametersResolver getParametersResolver() {
        initalize();
        return new ParametersResolver();
    }

    private static class ParameterEntry {

        final ParameterEntry next;
        final String name;
        final Object value;

        public ParameterEntry(String name, Object value, ParameterEntry next) {
            this.next = next;
            this.name = name;
            this.value = value;
        }
    }

    private class ParametersResolver {

        private final List<String> profiles = new ArrayList<String>();

        public void resolve(final Map<String, Object> result, final String beanName) {

            if (this.profiles.contains(beanName)) {
                return;
            }
            this.profiles.add(beanName);
            {
                //resolve x.@extends
                final Object extendsString;
                if ((extendsString = parameters.get(beanName.concat(".@extends"))) != null) {
                    final String[] extendsProfiles = StringUtil.splitc(String.valueOf(extendsString), ',');
                    StringUtil.trimAll(extendsProfiles);
                    String profile;
                    for (int i = 0, len = extendsProfiles.length; i < len; i++) {
                        if ((profile = extendsProfiles[i]).length() != 0) {
                            resolve(result, profile);
                        }
                    }
                }
            }

            for (ParameterEntry entry = parameterEntrys.get(beanName);
                    entry != null;
                    entry = entry.next) {
                result.put(entry.name, entry.value);
            }
        }
    }
}
