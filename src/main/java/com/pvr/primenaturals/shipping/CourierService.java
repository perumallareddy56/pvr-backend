package com.pvr.primenaturals.shipping;

import com.pvr.primenaturals.entity.Courier;
import com.pvr.primenaturals.entity.Order;
import java.util.List;

public interface CourierService {
    List<Courier> getActiveCouriers();
    String assignCourierAndTracking(Order order);
}
