package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Book extends Model {
    
    @Required
    public String title;
    @Required
    public Integer year;

    @Required
    @ManyToOne // 必须 标注 ManyToOne 表达外键关系
    public Contact author;
    
    // optional fields
    public Long votes;
    public Integer rank;
    public Float rating = 6.0f;
    
    public Boolean available;
}

