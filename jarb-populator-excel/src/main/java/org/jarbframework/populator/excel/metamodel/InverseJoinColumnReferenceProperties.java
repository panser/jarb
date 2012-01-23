package org.jarbframework.populator.excel.metamodel;

import java.util.Collection;

import javax.persistence.metamodel.EmbeddableType;

import org.apache.poi.hssf.record.formula.functions.T;

/**
 * Data structure for storing information needed for an inversed JoinColumn relation (ElementCollection or OneToMany with JoinColumn).
 * @author Sander Benschop
 *
 */
public final class InverseJoinColumnReferenceProperties {

    private InverseJoinColumnReferenceType inverseJoinColumnReferenceType = InverseJoinColumnReferenceType.SERIALIZABLE_CLASS;

    private EmbeddableType<T> embeddableType;
    private String referencedTableName;
    private Collection<String> joinColumnNames;

    /**
     * Returns the destination tablename of the inversed JoinColumn relation.
     * @return Destination tablename
     */
    public String getReferencedTableName() {
        return referencedTableName;
    }

    /**
     * Sets the destination tablename of the inversed JoinColumn relation.
     * @param referencedTableName The destination tablename
     */
    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    /**
     * Returns the @JoinColumn names the destination table uses the refer back to its owner record.
     * @return @JoinColumn names
     */
    public Collection<String> getJoinColumnNames() {
        return joinColumnNames;
    }

    /**
     * Returns the @JoinColumn names the destination table uses the refer back to its owner record.
     * @param joinColumnNames @JoinColumn names
     */
    public void setJoinColumnNames(Collection<String> joinColumnNames) {
        this.joinColumnNames = joinColumnNames;
    }

    public void setInverseJoinColumnReferenceType(InverseJoinColumnReferenceType inverseJoinColumnReferenceType) {
        this.inverseJoinColumnReferenceType = inverseJoinColumnReferenceType;
    }

    public InverseJoinColumnReferenceType getInverseJoinColumnReferenceType() {
        return inverseJoinColumnReferenceType;
    }

    public void setEmbeddableType(EmbeddableType<T> embeddableType) {
        this.embeddableType = embeddableType;
    }

    public EmbeddableType<T> getEmbeddableType() {
        return embeddableType;
    }

}
