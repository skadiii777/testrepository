package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 业务汇报对象 biz_report
 *
 * @author biz
 */
public class Report extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    /** 汇报类型 */
    @Excel(name = "汇报类型", readConverterExp = "1=日报,2=周报,3=月报")
    private String reportType;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 汇报内容 */
    private String content;

    /** 汇报日期 */
    @Excel(name = "汇报日期")
    private String reportDate;

    public void setReportType(String reportType)
    {
        this.reportType = reportType;
    }

    public String getReportType()
    {
        return reportType;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContent()
    {
        return content;
    }

    public void setReportDate(String reportDate)
    {
        this.reportDate = reportDate;
    }

    public String getReportDate()
    {
        return reportDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("reportType", getReportType())
            .append("title", getTitle())
            .append("content", getContent())
            .append("reportDate", getReportDate())
            .toString();
    }
}
