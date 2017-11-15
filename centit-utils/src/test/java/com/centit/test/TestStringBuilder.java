package com.centit.test;

import com.centit.support.algorithm.StringBaseOpt;
import com.centit.support.algorithm.StringRegularOpt;

public class TestStringBuilder {

    public static void main(String[] args) {
        System.out.println(StringBaseOpt.midPad("12",6,"UX",'0'));
        System.out.println(StringBaseOpt.midPad("adc",0,null,"X"));
        System.out.println(StringBaseOpt.midPad("def",6,null,null));
        System.out.println(StringRegularOpt.isMatch(
                "江苏南京东南大学先腾信息产业有限苏州分公司", "%南___大%公司%") );
    }

    public static void testString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(" select  p.vc_con_type  as contype, ");
        sb.append("    case p.vc_con_type when '1' then f.unitname else '' end as dept, ");
        sb.append("    case p.vc_con_type when '2' then u.username else '' end as teammember, ");
        sb.append("    p.vc_user_type          as usertype, ");
        sb.append("    p.l_before_invest_ratio as investbefore, ");
        sb.append("    p.l_after_invest_ratio  as investafter, ");
        sb.append("    p.l_quit_invest_ratio   as investquit ");
        sb.append(" from  P_PRO_CONTRIBUTE_DEGRE_HIS p ");
        sb.append(" left join F_UNITINFO f on f.unitcode = p.vc_dep_id  ");
        sb.append(" left join F_USERINFO u on u.usercode = p.vc_user_id  ");
        sb.append(" where p.vc_target_id = ?  and p.vc_change_id = ? ");
        sb.append(" order by p.vc_dep_id,p.vc_con_type,p.vc_user_type ");


        System.out.println(sb);

        String s= " select  p.vc_con_type  as contype, "+
        "    case p.vc_con_type when '1' then f.unitname else '' end as dept, "+
        "    case p.vc_con_type when '2' then u.username else '' end as teammember, "+
        "    p.vc_user_type          as usertype, "+
        "    p.l_before_invest_ratio as investbefore, "+
        "    p.l_after_invest_ratio  as investafter, "+
        "    p.l_quit_invest_ratio   as investquit "+
        " from  P_PRO_CONTRIBUTE_DEGRE_HIS p "+
        " left join F_UNITINFO f on f.unitcode = p.vc_dep_id  "+
        " left join F_USERINFO u on u.usercode = p.vc_user_id  "+
        " where p.vc_target_id = ?  and p.vc_change_id = ? "+
        " order by p.vc_dep_id,p.vc_con_type,p.vc_user_type ";

        System.out.println(s);
    }
}
