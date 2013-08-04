/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.metadata;

import static ma.glasnost.orika.metadata.FieldMapResult.ActionTaken.MAPPED;
import static ma.glasnost.orika.metadata.MappingDirection.A_TO_B;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ma.glasnost.orika.MappedTypePair;
import ma.glasnost.orika.Mapper;

/**
 * ClassMap represents a mapping association from one type to another.
 * 
 * @param <A>
 * @param <B>
 */
public class ClassMap<A, B> implements MappedTypePair<A, B> {
    
    private final Type<A> aType;
    private final Type<B> bType;
    private final Set<FieldMap> fieldMappings;
    private final Set<MapperKey> usedMappers;
    
    private final Mapper<A, B> customizedMapper;
    
    private final String[] constructorA;
    private final String[] constructorB;
    private final MapperKey mapperKey;
    
    private final Boolean sourcesMappedOnNull;
    private final Boolean destinationsMappedOnNull;
    
    private final Map<String, FieldMapResult> forwardMappingResult;
    private final Map<String, FieldMapResult> reverseMappingResult;
    
    /**
     * Constructs a new ClassMap
     * 
     * @param aType
     *            the 'A' type
     * @param bType
     *            the 'B' type
     * @param fieldsMapping
     *            the specific mapping of the fields from type 'A' to type 'B'
     *            and vise-versa
     * @param customizedMapper
     *            the customized mapper that should be used
     * @param usedMappers
     *            the set of mappers used by this mapper to map ancestors'
     *            fields
     * @param constructorA
     *            a description of the parameter names of the constructor to use
     *            for type 'A'
     * @param constructorB
     *            a description of the parameter names of the constructor to use
     *            for type 'B'
     * @param sourcesMappedOnNull
     *            specifies whether source fields should be mapped on null
     *            destination values (the reverse direction)
     * @param destinationsMappedOnNull
     *            specifies whether destination fields should be mapped on null
     *            source values
     * @param expectations
     *            the expectations associated with this class-map
     */
    public ClassMap(Type<A> aType, Type<B> bType, Set<FieldMap> fieldsMapping, Mapper<A, B> customizedMapper, Set<MapperKey> usedMappers,
            String[] constructorA, String[] constructorB, Boolean sourcesMappedOnNull, Boolean destinationsMappedOnNull) {
        this.aType = aType;
        this.bType = bType;
        
        this.customizedMapper = customizedMapper;
        
        this.fieldMappings = Collections.unmodifiableSet(fieldsMapping);
        this.usedMappers = Collections.unmodifiableSet(usedMappers);
        
        this.mapperKey = new MapperKey(aType, bType);
        
        this.sourcesMappedOnNull = sourcesMappedOnNull;
        this.destinationsMappedOnNull = destinationsMappedOnNull;
        
        this.forwardMappingResult = new LinkedHashMap<String, FieldMapResult>();
        this.reverseMappingResult = new LinkedHashMap<String, FieldMapResult>();
        
        if (constructorA != null) {
            this.constructorA = constructorA.clone();
        } else {
            this.constructorA = null;
        }
        
        if (constructorB != null) {
            this.constructorB = constructorB.clone();
        } else {
            this.constructorB = null;
        }
    }
    
    /**
     * Creates a copy of this ClassMap, replacing the specified set of FieldMaps
     * 
     * @param fieldsMapping
     * @return a copy of this ClassMap using the provided set of FieldMap
     *         instances instead of the existing set
     */
    public ClassMap<A, B> copy(Set<FieldMap> fieldsMapping) {
        Set<MapperKey> usedMappers = new LinkedHashSet<MapperKey>();
        usedMappers.addAll(this.usedMappers);
        String[] constructorA = this.constructorA == null ? null : this.constructorA.clone();
        String[] constructorB = this.constructorB == null ? null : this.constructorB.clone();
        
        return new ClassMap<A, B>(aType, bType, fieldsMapping, customizedMapper, usedMappers, constructorA, constructorB,
                sourcesMappedOnNull, destinationsMappedOnNull);
    }
    
    /**
     * @return the MapperKey which uniquely identifies the set of types mapped
     *         by this ClassMap
     */
    public MapperKey getMapperKey() {
        return mapperKey;
    }
    
    /**
     * @param fieldMap
     */
    public void addFieldMap(FieldMap fieldMap) {
        fieldMappings.add(fieldMap);
    }
    
    /**
     * @return the 'A' type for the mapping
     */
    public Type<A> getAType() {
        return aType;
    }
    
    /**
     * @return the 'B' type for the mapping
     */
    public Type<B> getBType() {
        return bType;
    }
    
    /**
     * @return the mapping of fields between the two types of this mapping
     */
    public Set<FieldMap> getFieldsMapping() {
        return fieldMappings;
    }
    
    /**
     * @return the name of the 'A' type
     */
    public String getATypeName() {
        return aType.getSimpleName();
    }
    
    /**
     * @return the name of the 'B' type
     */
    public String getBTypeName() {
        return bType.getSimpleName();
    }
    
    /**
     * @return the customized Mapper to be used for this mapping
     */
    public Mapper<A, B> getCustomizedMapper() {
        return customizedMapper;
    }
    
    /**
     * @return the class-name that should be used for the generated mapper class
     */
    public String getMapperClassName() {
        return "Orika_" + getBTypeName() + "_" + getATypeName() + "_Mapper";
    }
    
    /**
     * @return the set of field names used to identify the constructor to be
     *         used when generating instances of type A
     */
    public String[] getConstructorA() {
        return constructorA;
    }
    
    /**
     * @return the set of field names used to identify the constructor to be
     *         used when generating instances of type B
     */
    public String[] getConstructorB() {
        return constructorB;
    }
    
    /**
     * @return sourcesMappedOnNull for this ClassMap; can be null, which
     *         indicates that no preference is specified, and the global default
     *         should be used
     */
    public Boolean areSourcesMappedOnNull() {
        return sourcesMappedOnNull;
    }
    
    /**
     * @return destinationsMappedOnNull for this ClassMap; can be null, which
     *         indicates that no preference is specified, and the global default
     *         should be used
     */
    public Boolean areDestinationsMappedOnNull() {
        return destinationsMappedOnNull;
    }
    
    @Override
    public int hashCode() {
        int result = 31;
        result = result + (aType == null ? 0 : aType.hashCode());
        result = result + (bType == null ? 0 : bType.hashCode());
        return result;
    }
    
    /**
     * @return the set of MapperKey instances describing the Mappers inherited
     *         for use by this class-map
     */
    public Set<MapperKey> getUsedMappers() {
        return usedMappers;
    }
    
    /**
     * @return the forwardMappingResult
     */
    protected Map<String, FieldMapResult> getForwardMappingResults() {
        return forwardMappingResult;
    }
    
    /**
     * @return the reverseMappingResult
     */
    protected Map<String, FieldMapResult> getReverseMappingResults() {
        return reverseMappingResult;
    }
    
    /**
     * Evaluates all of the expectations associated with this ClassMap
     */
    public void evaluateMappingResults() {
        validate(MappingDirection.A_TO_B);
        validate(MappingDirection.B_TO_A);
    }
    
    /**
     * @param direction
     * @throws FailedExpectationException
     */
    public void validate(MappingDirection direction) throws FailedExpectationException {
        
        Map<String, Property> remainingExpectedFields = new HashMap<String, Property>();
        Map<String, Property> remainingExcludedFields = new HashMap<String, Property>();
        for (FieldMap fieldMap : fieldMappings) {
            if (direction.includes(fieldMap.getDirection())) {
                Property src = direction == A_TO_B ? fieldMap.getSource() : fieldMap.getDestination();
                Property dst = direction == A_TO_B ? fieldMap.getDestination() : fieldMap.getSource();
                if (fieldMap.isExcluded()) {
                    remainingExcludedFields.put(dst.getExpression(), dst);
                } else if (!fieldMap.isIgnored()) {
                    if (src.getGetter() != null && dst.getSetter() != null) {
                        remainingExpectedFields.put(dst.getExpression(), dst);
                    }
                }
            }
        }
        
        Map<String, FieldMapResult> results = direction == A_TO_B ? getForwardMappingResults() : getReverseMappingResults();
        for (Entry<String, FieldMapResult> entry : results.entrySet()) {
            FieldMapResult result = entry.getValue();
            FieldMap fieldMap = result.getFieldMap();
            
            if (remainingExpectedFields.containsKey(fieldMap.getDestinationExpression())) {
                if (MAPPED != result.getActionTaken()) {
                    throw failedExpectation("although it was expected to be mapped, '" + fieldMap.getDestinationExpression() + "' was "
                            + result.getActionTaken().toString().toLowerCase() + " because " + result.getComment(), this,
                            fieldMap.getSource(), fieldMap.getDestination());
                }
                remainingExpectedFields.remove(fieldMap.getDestinationExpression());
            } else if (remainingExcludedFields.containsKey(fieldMap.getDestinationExpression())) {
                if (MAPPED == result.getActionTaken()) {
                    throw failedExpectation("although it was expected to be exluded, '" + fieldMap.getDestinationExpression() + "' was "
                            + result.getActionTaken().toString().toLowerCase() + (fieldMap.isByDefault() ? " by default" : " explicitly")
                            + ", by " + result.getComment(), this, fieldMap.getSource(), fieldMap.getDestination());
                }
                remainingExcludedFields.remove(fieldMap.getDestinationExpression());
            } else {
                if (MAPPED == result.getActionTaken()) {
                    throw failedExpectation("although it was not expected, '" + fieldMap.getDestinationExpression() + "' was "
                            + result.getActionTaken().toString().toLowerCase() + (fieldMap.isByDefault() ? " by default" : " explicitly")
                            + ", by " + result.getComment(), this, fieldMap.getSource(), fieldMap.getDestination());
                }
            }
            
        }
    }
    
    /**
     * Constructs a new FailedExpectationException using the provided details
     * 
     * @param message
     * @param classMap
     * @param source
     * @param dest
     * @return
     */
    private FailedExpectationException failedExpectation(String message, ClassMap<?, ?> classMap, Property source, Property dest) {
        FailedExpectationException e = new FailedExpectationException(message);
        e.setDestinationType(classMap.getBType());
        e.setSourceType(classMap.getAType());
        e.setDestinationProperty(dest);
        e.setSourceProperty(source);
        return e;
    }
    
    public String toString() {
        return getClass().getSimpleName() + "([A]:" + aType + ", [B]:" + bType + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassMap<?, ?> other = (ClassMap<?, ?>) obj;
        if (aType == null) {
            if (other.aType != null) {
                return false;
            }
        } else if (!aType.equals(other.aType)) {
            return false;
        }
        if (bType == null) {
            if (other.bType != null) {
                return false;
            }
        } else if (!bType.equals(other.bType)) {
            return false;
        }
        return true;
    }
    
    /**
     * ReversedClassMapProxy provides access to the details of the specified
     * ClassMap in the reverse direction
     * 
     * @author mattdeboer
     * 
     * @param <A>
     * @param <B>
     */
    public static class ReversedClassMapProxy<A, B> extends ClassMap<A, B> {
        
        private final ClassMap<B, A> reverse;
        private final Set<FieldMap> reversedFieldsMapping;
        
        /**
         * @param classMap
         */
        private ReversedClassMapProxy(ClassMap<B, A> classMap) {
            super(classMap.getBType(), classMap.getAType(), Collections.<FieldMap> emptySet(), null, Collections.<MapperKey> emptySet(),
                    classMap.getConstructorB(), classMap.getConstructorA(), classMap.destinationsMappedOnNull,
                    classMap.sourcesMappedOnNull);
            reverse = classMap;
            reversedFieldsMapping = new LinkedHashSet<FieldMap>();
            for (FieldMap f : reverse.getFieldsMapping()) {
                reversedFieldsMapping.add(f.flip());
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.metadata.ClassMap#copy(java.util.Set)
         */
        public ClassMap<A, B> copy(Set<FieldMap> fieldsMapping) {
            throw new UnsupportedOperationException();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * ma.glasnost.orika.metadata.ClassMap#addFieldMap(ma.glasnost.orika
         * .metadata.FieldMap)
         */
        public void addFieldMap(FieldMap fieldMap) {
            throw new UnsupportedOperationException();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.metadata.ClassMap#getFieldsMapping()
         */
        public Set<FieldMap> getFieldsMapping() {
            return reversedFieldsMapping;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.metadata.ClassMap#getCustomizedMapper()
         */
        public Mapper<A, B> getCustomizedMapper() {
            throw new UnsupportedOperationException();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.metadata.ClassMap#getUsedMappers()
         */
        public Set<MapperKey> getUsedMappers() {
            throw new UnsupportedOperationException();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.metadata.ClassMap#assertExpectationsMet()
         */
        public void evaluateMappingResults() {
            throw new UnsupportedOperationException();
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.metadata.ClassMap#flip()
         */
        public ClassMap<B, A> flip() {
            return reverse;
        }

        /* (non-Javadoc)
         * @see ma.glasnost.orika.metadata.ClassMap#getForwardMappingResults()
         */
        @Override
        protected Map<String, FieldMapResult> getForwardMappingResults() {
            return reverse.getReverseMappingResults();
        }

        /* (non-Javadoc)
         * @see ma.glasnost.orika.metadata.ClassMap#getReverseMappingResults()
         */
        @Override
        protected Map<String, FieldMapResult> getReverseMappingResults() {
            return reverse.getForwardMappingResults();
        }
    }
    
    /**
     * @return a reversed proxy of the current ClassMap; not all operations are
     *         available
     */
    public ClassMap<B, A> flip() {
        return new ReversedClassMapProxy<B, A>(this);
    }
}
