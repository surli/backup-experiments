package com.evolveum.midpoint.repo.sql.data.audit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@IdClass(RAuditItemId.class)
@Table(name = RAuditItem.TABLE_NAME, indexes = {
		@Index(name = "iChangedItemPath", columnList = "changedItemPath")})
public class RAuditItem {

	public static final String TABLE_NAME = "m_audit_item";
	public static final String COLUMN_RECORD_ID = "record_id";
	
    private RAuditEventRecord record;
    private Long recordId;
    private String changedItemPath;

    
    @ForeignKey(name = "none")
    @MapsId("record")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = COLUMN_RECORD_ID, referencedColumnName = "id")
    })
    public RAuditEventRecord getRecord() {
        return record;
    }

    @Id
    @Column(name = COLUMN_RECORD_ID)
    public Long getRecordId() {
        if (recordId == null && record != null) {
            recordId = record.getId();
        }
        return recordId;
    }
   
    @Id
    @Column(name = "changedItemPath", length=900)
    public String getChangedItemPath() {
		return changedItemPath;
	}
    
    public void setRecord(RAuditEventRecord record) {
		if (record.getId() != 0) {
			this.recordId = record.getId();
		}
    	this.record = record;
	}
    
   
    public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}
    
    public void setChangedItemPath(String changedItemPath) {
		this.changedItemPath = changedItemPath;
	}
    
    public static RAuditItem toRepo(RAuditEventRecord record, String itemPath) {
    	RAuditItem itemChanged = new RAuditItem();
    	itemChanged.setRecord(record);
    	itemChanged.setChangedItemPath(itemPath);
    	return itemChanged;
    	
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RAuditItem that = (RAuditItem) o;

        if (changedItemPath != null ? !changedItemPath.equals(that.changedItemPath) : that.changedItemPath != null) return false;
//        if (record != null ? !record.equals(that.record) : that.record != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result1 = changedItemPath != null ? changedItemPath.hashCode() : 0;
//        result1 = 31 * result1 + (record != null ? record.hashCode() : 0);
        return result1;
    }

	
}
