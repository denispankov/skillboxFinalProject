package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.pankov.dto.search.SearchResultInterface;
import ru.pankov.entities.SiteEntity;
import ru.pankov.dto.search.SearchResult;
import ru.pankov.dto.statistic.SitesDetailStatistic;
import ru.pankov.dto.statistic.TotalStatistic;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    public static final String SEARCH_SQL = """
            with quantity_lemmas as (select count(1) cnt
                                       from lemma l
                                      where l.site_id in (select s.id from 
                                                            site s 
                                                           where s.name = :site or :site = '')
            ),
            lemmas as (select l.lemma 
                              ,l.frequency 
                              ,l.id 
                         from lemma l
                         join site s on s.id  = l.site_id 
                        where l.lemma in (:lemmasList)
                          and (s.\"name\"  =  :site or '' = :site)
            ),
            tempor as (select l.lemma 
                              ,l.frequency 
                              ,l.frequency / (select cnt from quantity_lemmas) percent
                              ,l.id
                         from lemmas l),
             res as (select string_agg(t.lemma, ', ') agg
                            ,sum(i.\"rank\") rel
                            ,count(1) cnt
                            ,(select  count(1) from lemmas) cnt_all
                            ,i.page_id 
                       from tempor t
                       join \"index\" i on i.lemma_id = t.id
                      where t.percent <= 0.065
                      group by i.page_id),
             resul as (select r.agg
                              ,r.cnt
                              ,r.cnt_all
                              ,r.rel / (select max(r.rel) from res r) rel_rel
                              ,r.page_id
                              ,count(1) over(partition by 1)  quant_page
                         from res r
                     order by cnt desc,5 desc)
             select  p.path as uri
                    ,p.content as content
                    ,l.rel_rel as relevance
                    ,l.quant_page as quantPages
                    ,s.url as site
                    ,s.name as siteName
                from resul l
                join page p on p.id  = l.page_id
                join site s on s.id  = p.site_id
                limit :limit offset :offset""";
    public static final String STATISTIC_TOTAL_SQL = """
            select new ru.pankov.pojo.statistic.TotalStatistic( (select count(1)
                      from site s) sites
                   ,(select count(1)
                       from page) pages
                   ,(select count(1)
                       from lemma) lemmas)
            """;
    public static final String STATISTIC_DETAIL_SQL = """
            select s.url
                  ,s.name
                  ,s.status
                  ,s.status_time
                  ,s.last_error
                  ,(select count(1)
                      from page p
                     where s.id = p.site_id ) pages
                  ,(select count(1)
                      from lemma l
                     where l.site_id  = s.id ) lemmas +
             from site s
            """;
    List<SiteEntity> findBySiteStatus(int siteStatus);

    SiteEntity findByName(String name);

    @Query(value = SEARCH_SQL, nativeQuery = true)
    List<SearchResultInterface> searchResult(@Param("site") String site, @Param("lemmasList") List<String> lemmasList, @Param("offset") int offset, @Param("limit") int limit);

    @Query(value = STATISTIC_TOTAL_SQL, nativeQuery = true)
    TotalStatistic getStatisticTotal();

    @Query(value = STATISTIC_DETAIL_SQL, nativeQuery = true)
    List<SitesDetailStatistic> getStatisticDetail();
}
