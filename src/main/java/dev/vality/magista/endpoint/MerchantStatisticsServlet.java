package dev.vality.magista.endpoint;

import dev.vality.magista.MerchantStatisticsServiceSrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/v3/stat")
public class MerchantStatisticsServlet extends GenericServlet {

    private Servlet thriftServlet;

    @Autowired
    private MerchantStatisticsServiceSrv.Iface requestHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(MerchantStatisticsServiceSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
