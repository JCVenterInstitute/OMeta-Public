/*
 * Copyright J. Craig Venter Institute, 2013
 *
 * The creation of this program was supported by J. Craig Venter Institute
 * and National Institute for Allergy and Infectious Diseases (NIAID),
 * Contract number HHSN272200900007C.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jcvi.ometa.configuration;

import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lfoster
 * Date: Oct 24, 2010
 * Time: 12:47:52 AM
 *
 * This class can look at a bean given it and determine exactly what fields can be set on it, and how to read
 * the required strings from a mapping.
 */
public class BeanPopulator {
    private static final String SET_PREFIX = "set";
    private static final Class OVERRIDE_ANNOTATION_CLASS = JCVI_BeanPopulator_Column.class;

    private Logger logger = Logger.getLogger(BeanPopulator.class);
    private Map<Method,String> methodVsDataName;
    private Class checkClass;

    /**
     * Constructor will build up mapping of methods versus desired/required header names for populating
     * beans, later.
     *
     * @param beanType only this type can be used by this populator.
     * @throws Exception thrown by any called methods.
     */
    public BeanPopulator(Class beanType) throws Exception {
        checkClass = beanType;
        Method[] methods = beanType.getDeclaredMethods();
        methodVsDataName = new HashMap<Method,String>();

        for(Method method: methods) {
            if(method.getName().startsWith(SET_PREFIX)) {

                // Only use "setters" that are annotated for use here.
                String putativeDataName = getHeaderName(method);
                if(putativeDataName != null) {
                    methodVsDataName.put(method, putativeDataName);
                }

            }
        }

    }

    /**
     * This returns a header set based on what is implied by the setters on the bean type.
     *
     * @return unique set of headers.
     */
    public Set<String> getDefaultHeaderSet() {
        Set returnSet = new HashSet<String>();
        returnSet.addAll(methodVsDataName.values());
        if(returnSet.size()   !=   methodVsDataName.size()) {
            StringBuilder message = new StringBuilder("Non-unique set of headers mapped to methods: [");
            for(String dataName: methodVsDataName.values()) {
                message.append(dataName).append(" ");
            }
            message.append("]");
            throw new IllegalArgumentException();
        }

        return returnSet;
    }

    /**
     * This populator will have deduced header names from its inputs.  This method lets those be
     * provided to client classes.
     *
     * @return list of headers expected by the populator.
     */
    public List<String> getHeaderNames() {
        List<String> rtnList = new ArrayList<String>();
        for(String headerName : methodVsDataName.values()) {
            rtnList.add(headerName);
        }
        return rtnList;
    }

    /**
     * Go through all methods in the bean, to learn how to look up their feed values.  Will only look at
     * "set"-ers defined on the class itself--not defined on ancestors.
     *
     * @param data mapping with all the data to be pushed into the setter methods.
     * @param bean has setter methods to be used in populating the bean.  Bag-o-data.
     * @throws Exception thrown if values cannot be set.
     */
    public void populateBean(Map<String,String> data, Object bean) throws Exception {
        // Need to look at the bean to see what information it is requesting.
        if(bean == null || data == null) {
            throw new IllegalArgumentException("Insufficient data: either data map or bean object is null.");
        }
        if(bean.getClass() != checkClass) {
            throw new IllegalArgumentException("This populator only works on beans of type " + checkClass.getName());
        }
        Method[] methods = bean.getClass().getDeclaredMethods();
        for(Method method: methods) {
            if(method.getName().startsWith(SET_PREFIX)) {

                String putativeDataName = methodVsDataName.get(method);

                // Get the data from the data map.
                String valueStr = data.get(putativeDataName);
                if(valueStr != null) {
                    invokeOneParamMethodWithProperType(method, bean, valueStr);
                }

            }
        }
    }

    /**
     * Uses introspection to find the name of the header to use in translation.
     *
     * @param method a method to be called.  Will provide clues as to what data to use to invoke it.
     * @return name of header to use, from input map.
     */
    private String getHeaderName(Method method) {
        // Get the expected name of the data, suitable for a mapping key.
        String putativeDataName = null;
        Annotation overridingAnnotation = getAnnotation(method);
        if(overridingAnnotation != null) {
            JCVI_BeanPopulator_Column headerMapping = (JCVI_BeanPopulator_Column)overridingAnnotation;
            putativeDataName = headerMapping.value();

            // No overridden column name.  Make assumption.
            if(putativeDataName == null  ||  putativeDataName.trim().length() == 0) {
                // Take the mixed-case name of the thing and use it.
                putativeDataName = method.getName().substring(SET_PREFIX.length());
                putativeDataName = putativeDataName.substring(0, 1).toUpperCase() + putativeDataName.substring(1);
            }
        }

        return putativeDataName;
    }

    /**
     * Find the annotation of interest and return that, or null if it is not found.
     *
     * @param method find it on this.
     * @return the overriding annotation, or null.
     */
    private Annotation getAnnotation(Method method) {
        Annotation overridingAnnotation = method.getAnnotation(OVERRIDE_ANNOTATION_CLASS);
        return overridingAnnotation;
    }

    /**
     * Assumptions:
     * 1. Method should have exactly one parameter.
     * 2. The value string should be legally convertible to the type of the parameter.
     * 3. It is legal to call this method.
     * Any non-conformance to these assumptions will lead to an exception being thrown.
     *
     * @param method a setter to invoke.
     * @param valueStr setter better have only one parameter.
     * @throws IllegalAccessException thrown by called code.
     * @throws InvocationTargetException thrown by called code.
     */
    private void invokeOneParamMethodWithProperType(Method method, Object bean, String valueStr) throws IllegalAccessException, InvocationTargetException {

        if(valueStr == null) {
            throw new IllegalArgumentException("Null given as value");
        }

        if(method == null) {
            throw new IllegalArgumentException("Null given as method.");
        }

        // Work out what type it needs to be.  Expecting float, double, int, long, or string.
        Class[] parameterTypes = method.getParameterTypes();
        if(parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                    "Setter encountered with wrong number of parameters for column " + userFyMethodName(method.getName()) + ", or method " + method.getName()
            );
        }
        Class setType = parameterTypes[ 0 ];
        if(setType.equals(Long.class)) {
            Long value = Long.parseLong(valueStr);
            method.invoke(bean, value);
        } else if(setType.equals(Integer.class)) {
            if(!valueStr.equals("")) {
                Integer value = Integer.parseInt(valueStr);
                method.invoke(bean, value);
            }
        } else if(setType.equals(Float.class)) {
            Float value = Float.parseFloat(valueStr);
            method.invoke(bean, value);
        } else if(setType.equals(Double.class)) {
            Double value = Double.parseDouble(valueStr);
            method.invoke(bean, value);
        } else if(setType.equals(Boolean.class)) {
            if(valueStr.length() == 0) {
                throw new RuntimeException("Expected boolean value, but received empty string, for col / method " + userFyMethodName(method.getName()) + ".");
            }
            Character testChar = valueStr.toUpperCase().charAt(0);
            Boolean value = testChar == 'Y' || testChar == 'T' || testChar == '1';
            method.invoke(bean, value);
        } else if(setType.equals(String.class)) {
            method.invoke(bean, valueStr);
        } else {
            throw new IllegalArgumentException(
                    "Type parameter for column " + userFyMethodName(method.getName()) + " or method " + method.getName() + " not one of those expected."
            );
        }
    }

    private String userFyMethodName(String methodName) {
        if(methodName.length() >= 4 && methodName.startsWith("set")) {
            return methodName.substring(3);
        } else {
            return methodName;
        }
    }
}
