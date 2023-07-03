package ru.pankov.dto.search;

import lombok.Getter;
import lombok.Setter;
import ru.pankov.entities.PageEntity;

@Getter
@Setter
public class Page implements Comparable{
    private PageEntity pageEntity;
    private double absoluteRelevance;
    private double relativeRelevance;

    public Page(PageEntity pageEntity, double absoluteRelevance) {
        this.pageEntity = pageEntity;
        this.absoluteRelevance = absoluteRelevance;
    }

    @Override
    public int compareTo(Object o) {
        if (this.absoluteRelevance > Page.class.cast(o).absoluteRelevance){
            return 1;
        }else if(this.absoluteRelevance < Page.class.cast(o).absoluteRelevance){
            return -1;
        }else {
            return 0;
        }
    }
}
