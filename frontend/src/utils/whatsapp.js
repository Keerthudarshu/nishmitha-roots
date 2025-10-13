export const sendOrderToWhatsApp = (orderData) => {
  const phoneNumber = "919845651468"; // WhatsApp number
  
  const formatOrderMessage = (order) => {
    const {
      orderId,
      customerInfo,
      items,
      shippingAddress,
      paymentMethod,
      subtotal,
      shipping,
      discount,
      total
    } = order;
    
    let message = `🛍️ *NEW ORDER RECEIVED*\n\n`;
    message += `📋 *Order ID:* ${orderId}\n`;
    message += `👤 *Customer:* ${customerInfo.name}\n`;
    message += `📞 *Phone:* ${customerInfo.phone}\n`;
    message += `📧 *Email:* ${customerInfo.email}\n\n`;
    
    message += `🛒 *Items Ordered:*\n`;
    items.forEach((item, index) => {
      message += `${index + 1}. ${item.name}\n`;
      message += `   Qty: ${item.quantity} x ₹${item.price} = ₹${item.quantity * item.price}\n`;
    });
    
    message += `\n📍 *Shipping Address:*\n`;
    message += `${shippingAddress.name}\n`;
    message += `${shippingAddress.street}\n`;
    message += `${shippingAddress.city}, ${shippingAddress.state} - ${shippingAddress.pincode}\n`;
    message += `Phone: ${shippingAddress.phone}\n\n`;
    
    message += `💳 *Payment Method:* ${paymentMethod}\n\n`;
    
    message += `💰 *Order Summary:*\n`;
    message += `Subtotal: ₹${subtotal.toFixed(2)}\n`;
    message += `Shipping: ₹${shipping.toFixed(2)}\n`;
    if (discount > 0) {
      message += `Discount: -₹${discount.toFixed(2)}\n`;
    }
    message += `*Total: ₹${total.toFixed(2)}*\n\n`;
    
    message += `⏰ *Order Time:* ${new Date().toLocaleString()}\n`;
    
    return message;
  };
  
  const message = formatOrderMessage(orderData);
  const encodedMessage = encodeURIComponent(message);
  const phone = phoneNumber.replace(/[^0-9]/g, '');

  // Build possible URLs (native app, api web endpoint, wa.me)
  const nativeUrl = `whatsapp://send?phone=${phone}&text=${encodedMessage}`;
  const apiUrl = `https://api.whatsapp.com/send?phone=${phone}&text=${encodedMessage}`;
  const waMeUrl = `https://wa.me/${phone}?text=${encodedMessage}`;

  // helper to open a URL via a clicked anchor (more reliable for prefilled text in some browsers)
  const openViaAnchor = (url, target = '_blank') => {
    try {
      const a = document.createElement('a');
      a.href = url;
      a.target = target;
      a.rel = 'noopener noreferrer';
      // Some browsers (esp. mobile) preserve query params better when navigation comes from a user-initiated click
      document.body.appendChild(a);
      a.click();
      setTimeout(() => { try { document.body.removeChild(a); } catch (e) {} }, 1000);
      return true;
    } catch (e) {
      return false;
    }
  };

  const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

  try {
    if (isMobile) {
      // Try native first on mobile devices; if it fails the browser will stay on page so we fallback shortly after
      openViaAnchor(nativeUrl, '_self');
      // Fallback to api web URL after a short delay
      setTimeout(() => openViaAnchor(apiUrl, '_blank') || openViaAnchor(waMeUrl, '_blank'), 700);
    } else {
      // Desktop: open api.whatsapp which generally preserves prefilled text
      if (!openViaAnchor(apiUrl, '_blank')) {
        // final fallback
        openViaAnchor(waMeUrl, '_blank');
      }
    }
  } catch (err) {
    // As a last resort use window.location to force navigation
    try {
      window.location.href = apiUrl;
    } catch (e) {
      console.error('Failed to open WhatsApp link', e);
    }
  }
};

// New, more flexible helper used by checkout flow
export const sendOrderDetailsToWhatsApp = async (order, reviewData, user, location, phoneNumber = '919845651468') => {
  try {
    const customerName = user?.name || reviewData?.address?.name || 'Unknown Customer';
    const customerPhone = reviewData?.address?.phone || user?.phone || 'Not provided';
    const customerEmail = user?.email || 'Not provided';
    const customerId = user?.id || user?.userId || reviewData?.customerId || 'N/A';
    const locationInfo = location ? `Location: ${location.latitude}, ${location.longitude}` : 'Location not available';

    const formatWhatsAppAddress = (addr) => {
      if (!addr) return 'Address not available';
      return `${addr.name || ''}\n${addr.street || ''}${addr.landmark ? ', ' + addr.landmark : ''}\n${addr.city || ''}, ${addr.state || ''} - ${addr.pincode || ''}\nPhone: ${addr.phone || ''}`;
    };

    const orderItems = reviewData?.items || order?.items || [];

    const itemsText = orderItems.length > 0
      ? orderItems.map((item, index) => {
          const itemPrice = parseFloat(item?.price) || 0;
          const itemQuantity = parseInt(item?.quantity) || 0;
          const itemTotal = itemPrice * itemQuantity;
          return `${index + 1}. ${item?.name || 'Unknown Item'}\n   Qty: ${itemQuantity} x ₹${itemPrice.toFixed(2)} = ₹${itemTotal.toFixed(2)}`;
        }).join('\n\n')
      : 'No items found';

    const orderIdDisplay = order?.id ? `NN${order.id}` : (order?.orderId || 'N/A');

    const orderDetails = `🛍️ *NEW ORDER RECEIVED*\n\n` +
      `📋 *Order ID:* ${orderIdDisplay}\n` +
      `👤 *Customer:* ${customerName}\n` +
      `🆔 *Customer ID:* ${customerId}\n` +
      `📞 *Phone:* ${customerPhone}\n` +
      `📧 *Email:* ${customerEmail}\n\n` +
      `🛒 *Items Ordered:*\n` +
      `${itemsText}\n\n` +
      `📍 *Shipping Address:*\n` +
      `${formatWhatsAppAddress(reviewData?.address)}\n\n` +
      `${locationInfo}\n\n` +
      `💳 *Payment Method:* ${reviewData?.paymentMethod || 'Not specified'}\n` +
      `🚚 *Delivery Option:* ${reviewData?.deliveryOption || 'Standard'}\n\n` +
      `💰 *Order Summary:*\n` +
      `Subtotal: ₹${(reviewData?.subtotal ?? 0).toFixed ? reviewData?.subtotal?.toFixed(2) : Number(reviewData?.subtotal || 0).toFixed(2)}\n` +
      `Shipping: ₹${(reviewData?.shippingFee ?? 0).toFixed ? reviewData?.shippingFee?.toFixed(2) : Number(reviewData?.shippingFee || 0).toFixed(2)}\n` +
      `*Total: ₹${(reviewData?.total ?? 0).toFixed ? reviewData?.total?.toFixed(2) : Number(reviewData?.total || 0).toFixed(2)}*\n\n` +
      `⏰ *Order Time:* ${new Date().toLocaleString('en-IN')}\n\n` +
      `🎉 Thank you for your order! We'll process it shortly.`;

    const encodedMessage = encodeURIComponent(orderDetails);

    // Use api.whatsapp.com which is more consistent than wa.me for prefilled text
    const phone = (phoneNumber || '').toString().replace(/[^0-9]/g, '');
    const url = `https://api.whatsapp.com/send?phone=${phone}&text=${encodedMessage}`;

    // Try a multi-strategy open so prefilled text survives across mobile/desktop
    const nativeUrl = `whatsapp://send?phone=${phone}&text=${encodedMessage}`;
    const apiUrl = url; // already api.whatsapp.com url
    const waMeUrl = `https://wa.me/${phone}?text=${encodedMessage}`;

    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

    const openViaAnchor = (link, target = '_blank') => {
      try {
        const a = document.createElement('a');
        a.href = link;
        a.target = target;
        a.rel = 'noopener noreferrer';
        document.body.appendChild(a);
        a.click();
        setTimeout(() => { try { document.body.removeChild(a); } catch (e) {} }, 1000);
        return true;
      } catch (e) {
        return false;
      }
    };

    if (isMobile) {
      // try native then fallback to api
      openViaAnchor(nativeUrl, '_self');
      setTimeout(() => {
        if (!openViaAnchor(apiUrl, '_blank')) openViaAnchor(waMeUrl, '_blank');
      }, 700);
    } else {
      if (!openViaAnchor(apiUrl, '_blank')) {
        if (!openViaAnchor(waMeUrl, '_blank')) {
          throw new Error('Please allow popups to send order details to WhatsApp.');
        }
      }
    }
  } catch (err) {
    console.error('Failed to send WhatsApp message:', err);
    // swallow error to not block order success UI
  }
};
