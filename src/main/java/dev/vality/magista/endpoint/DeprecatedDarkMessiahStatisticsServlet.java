package dev.vality.magista.endpoint;

import dev.vality.damsel.merch_stat.DarkMessiahStatisticsSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@Deprecated
@WebServlet("/v2/stat")
public class DeprecatedDarkMessiahStatisticsServlet extends GenericServlet {

    private Servlet thriftServlet;

    @Autowired
    private DarkMessiahStatisticsSrv.Iface requestHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(DarkMessiahStatisticsSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}