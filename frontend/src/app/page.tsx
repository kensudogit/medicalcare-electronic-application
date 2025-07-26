'use client';

import { useState } from 'react';
import Link from 'next/link';
import { 
  BuildingOfficeIcon, 
  DocumentTextIcon, 
  UserGroupIcon,
  ChartBarIcon 
} from '@heroicons/react/24/outline';

export default function HomePage() {
  const [activeTab, setActiveTab] = useState('dashboard');

  const navigation = [
    { name: 'ダッシュボード', href: '#', icon: ChartBarIcon, current: activeTab === 'dashboard' },
    { name: '医療機関管理', href: '/institutions', icon: BuildingOfficeIcon, current: activeTab === 'institutions' },
    { name: '申請管理', href: '/applications', icon: DocumentTextIcon, current: activeTab === 'applications' },
    { name: 'ユーザー管理', href: '#', icon: UserGroupIcon, current: activeTab === 'users' },
  ];

  const stats = [
    { name: '登録医療機関数', value: '1,234', change: '+12%', changeType: 'positive' },
    { name: '申請件数（今月）', value: '567', change: '+8%', changeType: 'positive' },
    { name: '承認済み申請', value: '456', change: '+15%', changeType: 'positive' },
    { name: '保留中申請', value: '89', change: '-5%', changeType: 'negative' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-16 justify-between">
            <div className="flex">
              <div className="flex flex-shrink-0 items-center">
                <h1 className="text-xl font-bold text-gray-900">
                  医療系電子申請システム
                </h1>
              </div>
            </div>
            <div className="flex items-center">
              <button className="rounded-md bg-primary-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-primary-500">
                ログアウト
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex gap-6 py-6">
          {/* Sidebar */}
          <div className="w-64">
            <nav className="space-y-1">
              {navigation.map((item) => (
                <Link
                  key={item.name}
                  href={item.href}
                  className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md ${
                    item.current
                      ? 'bg-primary-100 text-primary-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                  onClick={() => setActiveTab(item.name.toLowerCase().replace(/\s+/g, ''))}
                >
                  <item.icon
                    className={`mr-3 h-6 w-6 flex-shrink-0 ${
                      item.current ? 'text-primary-500' : 'text-gray-400 group-hover:text-gray-500'
                    }`}
                    aria-hidden="true"
                  />
                  {item.name}
                </Link>
              ))}
            </nav>
          </div>

          {/* Main content */}
          <div className="flex-1">
            <div className="mb-8">
              <h2 className="text-2xl font-bold text-gray-900">ダッシュボード</h2>
              <p className="mt-1 text-sm text-gray-500">
                システムの概要と最新の申請状況をご確認いただけます。
              </p>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
              {stats.map((item) => (
                <div key={item.name} className="card">
                  <dt className="text-sm font-medium text-gray-500 truncate">{item.name}</dt>
                  <dd className="mt-1 text-3xl font-semibold text-gray-900">{item.value}</dd>
                  <dd className={`text-sm ${
                    item.changeType === 'positive' ? 'text-green-600' : 'text-red-600'
                  }`}>
                    {item.change}
                  </dd>
                </div>
              ))}
            </div>

            {/* Recent Applications */}
            <div className="mt-8">
              <h3 className="text-lg font-medium text-gray-900 mb-4">最近の申請</h3>
              <div className="card">
                <div className="overflow-hidden">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          申請番号
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          医療機関
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          申請タイプ
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          ステータス
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          申請日
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      <tr>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          APP-12345678
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          東京総合病院
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          新規開業届
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                            審査中
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          2024-01-15
                        </td>
                      </tr>
                      <tr>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          APP-87654321
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          青山内科クリニック
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          更新申請
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                            承認済み
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          2024-01-10
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 