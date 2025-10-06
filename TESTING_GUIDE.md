# End-to-End Testing Guide for Nishmitha Roots

## 🚀 Complete System Setup Verification

### Step 1: Verify CORS Fix
1. Open browser DevTools (F12)
2. Go to `https://nishmitha-roots.vercel.app`
3. Check Network tab - should see successful API calls without CORS errors

### Step 2: Seed the Database
**Method 1: Via API Call (Recommended)**
```bash
curl -X POST https://nishmitha-roots-7.onrender.com/api/dev/seed-users
```

**Method 2: Via Browser**
1. Go to: `https://nishmitha-roots-7.onrender.com/api/dev/status`
2. If shows empty database, use this URL: `https://nishmitha-roots-7.onrender.com/api/dev/seed-users` (POST request)

**Method 3: Via Frontend Console**
```javascript
fetch('https://nishmitha-roots-7.onrender.com/api/dev/seed-users', {method: 'POST'})
  .then(r => r.json())
  .then(console.log)
```

### Step 3: Test Admin Panel
1. **Go to**: `https://nishmitha-roots.vercel.app/admin-login`
2. **Login with**:
   - Email: `admin@gmail.com`
   - Password: `Admin@123`
3. **Verify admin panel loads** with backend data

### Step 4: Test User Registration/Login
1. **Go to**: `https://nishmitha-roots.vercel.app/user-auth`
2. **Test existing user**:
   - Email: `nishu@gmail.com`
   - Password: `Nishu@123`
3. **Test new user registration** with your own email

### Step 5: Verify API Connectivity
**Check these endpoints work from frontend:**
- ✅ Authentication: `/api/auth/login`, `/api/auth/register`
- ✅ Products: `/api/admin/products`
- ✅ Categories: `/api/categories`
- ✅ Cart: `/api/cart`
- ✅ Users: `/api/admin/users`

### Step 6: Database Verification
**Check database status**:
```
GET https://nishmitha-roots-7.onrender.com/api/dev/status
```
Should return:
```json
{
  "total_users": 2,
  "has_admin_user": true,
  "has_test_user": true,
  "database_ready": true
}
```

## 🔧 Troubleshooting

### CORS Issues
- Error: "CORS policy blocks request"
- Solution: Wait for Render deployment to complete (~3 minutes)

### Database Connection Issues  
- Error: "Communications link failure"
- Solution: Check if Neon database is active and connection string is correct

### Authentication Issues
- Error: "Invalid credentials"
- Solution: Run database seeding first (Step 2)

### Admin Panel Loading Issues
- Error: "Failed to load data"
- Solution: Verify CORS fix deployed and database seeded

## 📊 Expected Results

### Frontend (Vercel)
- ✅ Loads at: `https://nishmitha-roots.vercel.app`
- ✅ No CORS errors in browser console
- ✅ API calls succeed to Render backend

### Backend (Render)
- ✅ API accessible at: `https://nishmitha-roots-7.onrender.com/api`
- ✅ Database connected to Neon PostgreSQL
- ✅ Sample users created and accessible

### Admin Panel
- ✅ Login works with admin credentials
- ✅ Product management functions
- ✅ User management displays seeded users
- ✅ Real-time data from backend

## 🎯 Success Criteria
- [ ] Admin can login and manage products
- [ ] Users can register and login
- [ ] Shopping cart functionality works
- [ ] All API endpoints respond correctly
- [ ] No CORS errors in browser console
- [ ] Database operations persist correctly

## 🔐 Sample Credentials
**Admin User**
- Email: `admin@gmail.com`
- Password: `Admin@123`
- Role: `admin`

**Test User**
- Email: `nishu@gmail.com` 
- Password: `Nishu@123`
- Role: `user`

---
*Last Updated: October 3, 2025*
*Backend: https://nishmitha-roots-7.onrender.com*
*Frontend: https://nishmitha-roots.vercel.app*